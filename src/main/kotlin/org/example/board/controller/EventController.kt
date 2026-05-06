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

    // 💡 1. 행사 목록 전체 조회 (GET /api/events?page=0&size=10&sort=id,desc)
    @GetMapping
    fun getEvents(
        // 프론트에서 아무 조건도 안 보낼 경우 기본값: 한 페이지에 10개씩, 최신순(id 내림차순)
        @PageableDefault(size = 10, sort = ["id"], direction = Sort.Direction.DESC)
        pageable: Pageable
    ): ResponseEntity<Page<EventListResponse>> {

        val responsePage = eventService.getEventList(pageable)
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