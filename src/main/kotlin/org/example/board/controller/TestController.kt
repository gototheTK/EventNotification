package org.example.board.controller

import org.example.board.service.EventSyncService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class TestController(
    private val eventSyncService: EventSyncService // 우리가 만든 파이프라인 주입!
) {

    // 브라우저에서 주소창에 치면 바로 파이프라인이 실행되도록 연결
    @GetMapping("/api/test/sync-events")
    fun testSync(): String {
        eventSyncService.syncEventsFromSeoulApi()
        return "✅ 수동 동기화 완료! 스프링 부트 콘솔 로그와 DB를 확인해보세요."
    }
}