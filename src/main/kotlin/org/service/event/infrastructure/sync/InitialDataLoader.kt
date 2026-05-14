package org.service.event.infrastructure.sync

import org.service.event.domain.event.EventRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class InitialDataLoader(
    private val eventSyncService: EventSyncService,
    private val eventRepository: EventRepository
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @EventListener(ApplicationReadyEvent::class)
    fun loadInitialData() {
        if (eventRepository.count() == 0L) {
            log.info("🚀 DB가 비어있습니다. 초기 문화행사 데이터를 적재합니다...")
            eventSyncService.syncEventsFromSeoulApi()
            log.info("✅ 초기 데이터 적재 완료!")
        } else {
            log.info("ℹ️ 이미 DB에 데이터가 존재하여 초기 적재를 건너뜁니다.")
        }
    }
}
