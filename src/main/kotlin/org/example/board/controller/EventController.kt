package org.example.board.controller

import org.example.board.dto.EventDetailResponse
import org.example.board.dto.EventListResponse
import org.example.board.service.EventService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/events")
class EventController(
    private val eventService: EventService
) {

    // 💡 1. 행사 목록 전체 조회 및 검색을 하나로 통합! (GET /api/events?title=축제&page=0)
    @GetMapping
    fun getEvents(
        @RequestParam(required = false) title: String?,
        @RequestParam(required = false) codeName: String?,
        @RequestParam(required = false) guName: String?,
        @PageableDefault(size = 10, sort = ["id"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<Page<EventListResponse>> {

        // 검색어가 없으면(null) 자동으로 전체 목록이 반환되고, 있으면 필터링됩니다.
        val responsePage = eventService.searchEvents(title, codeName, guName, pageable)
        return ResponseEntity.ok(responsePage)
    }

    // 💡 2. 특정 행사 상세 조회 (GET /api/events/1)
    @GetMapping("/{eventId}")
    fun getEventDetail(
        @PathVariable eventId: Long
    ): ResponseEntity<EventDetailResponse> {

        val responseDetail = eventService.getEventDetail(eventId)
        return ResponseEntity.ok(responseDetail)
    }

    @GetMapping("/search")
    fun search(
        @RequestParam(required = false) title: String?,
        @RequestParam(required = false) codeName: String?,
        @RequestParam(required = false) guName: String?,
        @PageableDefault(size = 10, sort = ["id"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<Page<EventListResponse>> {
        val results = eventService.searchEvents(title, codeName, guName, pageable)
        return ResponseEntity.ok(results)
    }
}