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
import org.springframework.transaction.annotation.Transactional
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

    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    fun syncEventsFromSeoulApi() {
        val url = "$seoulApiBaseUrl/$openApiKey/json/culturalEventInfo/1/100/"
        log.info("🌐 서울시 문화행사 API 동기화 시작...")

        val response = withRetry(maxRetries = 3, delayMs = 2000) {
            restClient.get().uri(url).retrieve().body(SeoulEventResponse::class.java)
        }

        if (response?.culturalEventInfo?.row == null) {
            log.error("❌ API 재시도 최종 실패로 인해 동기화를 중단합니다.")
            return
        }

        val rows = response.culturalEventInfo.row
        var savedCount = 0

        for (row in rows) {
            if (eventRepository.existsByTitle(row.title)) continue

            val newEvent = Event(
                title = row.title,
                posterUrl = row.mainImg,
                category = EventCategory(row.codeName, row.themeCode),
                period = EventPeriod(
                    startDate = parseDateSafe(row.startDate),
                    endDate = parseDateSafe(row.endDate),
                    dateText = row.dateText,
                    proTime = row.proTime
                ),
                location = EventLocation(
                    placeName = row.place,
                    guName = row.guName,
                    locationPoint = buildPoint(row.longitude, row.latitude)
                ),
                usageInfo = EventUsageInfo(row.isFree, row.useFee, row.useTarget),
                detail = EventDetail(row.player, row.program, row.etcDesc),
                organization = EventOrganization(
                    row.orgName, row.inquiry, row.orgLink, row.hmpgAddr, row.ticket, row.rgstDate
                )
            )
            eventRepository.save(newEvent)
            savedCount++
        }

        log.info("✅ 서울시 문화행사 동기화 완료! (총 검토: ${rows.size}건, 신규 저장: ${savedCount}건)")
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

    private fun parseDateSafe(dateString: String): LocalDateTime =
        runCatching { LocalDateTime.parse(dateString, dateFormatter) }.getOrElse { e ->
            log.warn("⚠️ 날짜 파싱 실패 - 입력값: \"$dateString\" (${e.message}). LocalDateTime.now()로 대체합니다.")
            LocalDateTime.now()
        }
}