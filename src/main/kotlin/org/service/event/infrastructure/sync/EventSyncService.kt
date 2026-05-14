package org.service.event.infrastructure.sync

import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.Point
import org.locationtech.jts.geom.PrecisionModel
import org.service.event.domain.event.*
import org.service.event.domain.event.EventRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

@Service
class EventSyncService(
    private val eventRepository: EventRepository,
    @Value("\${open-api.seoul.key}") private val openApiKey: String,
    @Value("\${api.seoul.base-url}") private val seoulApiBaseUrl: String
) {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val restClient = RestClient.create()

    /**
     * 서울 열린데이터광장 API 날짜 형식: "yyyy-MM-dd HH:mm:ss.S" (예: "2024-05-01 00:00:00.0")
     * 방어적으로 소수점 1–9자리 및 소수점 없는 형식까지 모두 수용한다.
     */
    private val dateFormatter: DateTimeFormatter = DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd HH:mm:ss")
        .optionalStart()
        .appendLiteral('.')
        .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, false)
        .optionalEnd()
        .toFormatter()
    private val geometryFactory = GeometryFactory(PrecisionModel(), 4326)

    // @Transactional 제거: 네트워크 I/O(API 호출 + 재시도) 동안 DB 커넥션을 점유하지 않는다.
    // DB 쓰기는 saveAll() 내부의 @Transactional이 담당하여 트랜잭션 범위를 최소화한다.
    @Scheduled(cron = "0 0 3 * * ?")
    fun syncEventsFromSeoulApi() {
        val url = "$seoulApiBaseUrl/$openApiKey/json/culturalEventInfo/1/100/"
        log.info("🌐 서울시 문화행사 API 동기화 시작...")

        // ① 네트워크 I/O — DB 커넥션 미점유
        val response = withRetry(maxRetries = 3, delayMs = 2000) {
            restClient.get().uri(url).retrieve().body(SeoulEventResponse::class.java)
        }

        if (response?.culturalEventInfo?.row == null) {
            log.error("❌ API 재시도 최종 실패로 인해 동기화를 중단합니다.")
            return
        }

        val rows = response.culturalEventInfo.row

        // ② 중복 제외 후 신규 이벤트 수집 — 루프 안에서 개별 save() 금지
        //    날짜 파싱 실패 행은 mapNotNull로 스킵하여 역전 구간(startDate > endDate) 저장을 방지한다.
        val newEvents = rows
            .filterNot { eventRepository.existsByTitle(it.title) }
            .mapNotNull { row ->
                val startDate = parseDateSafe(row.startDate)
                val endDate   = parseDateSafe(row.endDate)
                if (startDate == null || endDate == null) {
                    log.warn("⚠️ 날짜 파싱 불가로 행 스킵 — 제목: \"${row.title}\"")
                    return@mapNotNull null
                }
                Event(
                    title = row.title,
                    posterUrl = row.mainImg,
                    category = EventCategory(row.codeName, row.themeCode),
                    period = EventPeriod(
                        startDate = startDate,
                        endDate   = endDate,
                        dateText  = row.dateText,
                        proTime   = row.proTime
                    ),
                    location = EventLocation(
                        placeName    = row.place,
                        guName       = row.guName,
                        locationPoint = buildPoint(row.longitude, row.latitude)
                    ),
                    usageInfo    = EventUsageInfo(row.isFree, row.useFee, row.useTarget),
                    detail       = EventDetail(row.player, row.program, row.etcDesc),
                    organization = EventOrganization(
                        row.orgName, row.inquiry, row.orgLink, row.hmpgAddr, row.ticket, row.rgstDate
                    )
                )
            }

        // ③ DB 쓰기 — saveAll() 단일 트랜잭션으로 Batch Insert
        if (newEvents.isNotEmpty()) {
            eventRepository.saveAll(newEvents)
        }

        log.info("✅ 서울시 문화행사 동기화 완료! (총 검토: ${rows.size}건, 신규 저장: ${newEvents.size}건)")
    }

    /**
     * API에서 넘어오는 경도/위도 문자열을 JTS Point(SRID 4326)로 변환합니다.
     * 값이 없거나 0인 경우 null을 반환하여 PostGIS 쿼리에서 안전하게 제외됩니다.
     */
    private fun buildPoint(longitudeStr: String?, latitudeStr: String?): Point? {
        val lon = longitudeStr?.toDoubleOrNull()?.takeIf { it != 0.0 } ?: return null
        val lat = latitudeStr?.toDoubleOrNull()?.takeIf { it != 0.0 } ?: return null
        return geometryFactory.createPoint(Coordinate(lon, lat))
    }

    private fun <T> withRetry(maxRetries: Int, delayMs: Long, block: () -> T?): T? {
        repeat(maxRetries) { attempt ->
            val result = runCatching { block() }
            if (result.isSuccess && result.getOrNull() != null) return result.getOrNull()

            val errorMsg = result.exceptionOrNull()?.message ?: "응답 데이터가 null 입니다."
            log.warn("⚠️ API 호출 실패 (${attempt + 1}/$maxRetries) : $errorMsg. ${delayMs}ms 후 재시도합니다.")
            if (attempt < maxRetries - 1) Thread.sleep(delayMs)
        }
        return null
    }

    // null 반환 시 호출 측에서 해당 행 전체를 스킵하도록 설계되어 있다.
    private fun parseDateSafe(dateString: String): LocalDateTime? =
        runCatching { LocalDateTime.parse(dateString, dateFormatter) }.getOrElse { e ->
            log.warn("⚠️ 날짜 파싱 실패 - 입력값: \"$dateString\" (${e.message})")
            null
        }
}