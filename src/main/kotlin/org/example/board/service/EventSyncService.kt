package org.example.board.service

import org.example.board.dto.SeoulEventResponse
import org.example.board.entity.Event
import org.example.board.entity.EventCategory
import org.example.board.entity.EventDetail
import org.example.board.entity.EventLocation
import org.example.board.entity.EventOrganization
import org.example.board.entity.EventPeriod
import org.example.board.entity.EventUsageInfo
import org.example.board.repository.EventRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestClient
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import org.slf4j.LoggerFactory

@Service
class EventSyncService(
    private val eventRepository: EventRepository,
    @Value("\${open-api.seoul.key}") private val openApiKey: String
) {
    private val log = LoggerFactory.getLogger(this::class.java)
    private val restClient = RestClient.create()
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:s.S")

    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    fun syncEventsFromSeoulApi() {
        val url = "http://openapi.seoul.go.kr:8088/$openApiKey/json/culturalEventInfo/1/100/"

        log.info("🌐 서울시 문화행사 API 동기화 시작...")

        // 💡 1. 커스텀 Retry 함수를 사용해 API 호출 (최대 3번 시도, 2초 대기)
        val response = withRetry(maxRetries = 3, delayMs = 2000) {
            restClient.get()
                .uri(url)
                .retrieve()
                .body(SeoulEventResponse::class.java)
        }

        // 3번 다 실패해서 null이 반환되었다면 스케줄러 안전하게 종료
        if (response?.culturalEventInfo?.row == null) {
            log.error("❌ API 재시도 최종 실패로 인해 동기화를 중단합니다.")
            return
        }

        val rows = response.culturalEventInfo.row
        var savedCount = 0

        // 💡 2. 데이터 병합 및 저장 로직 (이전과 동일)
        for (row in rows) {
            if (eventRepository.existsByTitle(row.title)) continue

            val newEvent = Event(
                title = row.title,
                posterUrl = row.mainImg,
                category = EventCategory(row.codeName, row.themeCode),
                period = EventPeriod(
                    parseDateSafe(row.startDate),
                    parseDateSafe(row.endDate),
                    row.dateText,
                    row.proTime
                ),
                location = EventLocation(
                    row.place,
                    row.guName,
                    row.latitude?.toDoubleOrNull(),
                    row.longitude?.toDoubleOrNull()
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

    // --- 💡 [핵심] 코틀린 고차 함수를 활용한 우아한 재시도 메서드 ---
    private fun <T> withRetry(maxRetries: Int, delayMs: Long, block: () -> T?): T? {
        var attempt = 1
        while (attempt <= maxRetries) {
            val result = runCatching { block() }

            if (result.isSuccess && result.getOrNull() != null) {
                return result.getOrNull() // 성공 시 바로 결과 반환
            }

            val errorMsg = result.exceptionOrNull()?.message ?: "응답 데이터가 null 입니다."
            log.warn("⚠️ API 호출 실패 ($attempt/$maxRetries) : $errorMsg. ${delayMs}ms 후 재시도합니다.")

            if (attempt == maxRetries) break

            Thread.sleep(delayMs) // 지정된 시간만큼 대기 후 재시도
            attempt++
        }
        return null // 모든 재시도 실패 시 null 반환
    }

    private fun parseDateSafe(dateString: String): LocalDateTime {
        return runCatching {
            LocalDateTime.parse(dateString, dateFormatter)
        }.getOrElse { LocalDateTime.now() }
    }
}