package org.example.board.init


import org.example.board.repository.EventRepository
import org.example.board.service.EventSyncService
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

    // 💡 서버가 완전히 구동되어 트래픽을 받을 준비가 되면 자동으로 실행됩니다.
    @EventListener(ApplicationReadyEvent::class)
    fun loadInitialData() {
        // 서버를 껐다 켤 때마다 API를 부르면 낭비이므로, DB가 비어있을 때만 실행되게 방어!
        if (eventRepository.count() == 0L) {
            log.info("🚀 DB가 비어있습니다. 초기 문화행사 데이터를 적재합니다...")
            eventSyncService.syncEventsFromSeoulApi()
            log.info("✅ 초기 데이터 적재 완료!")
        } else {
            log.info("ℹ️ 이미 DB에 데이터가 존재하여 초기 적재를 건너뜁니다.")
        }
    }
}