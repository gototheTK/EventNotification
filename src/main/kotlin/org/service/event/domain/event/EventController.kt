package org.service.event.domain.event

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

    @GetMapping
    fun getEvents(
        @RequestParam(required = false) title: String?,
        @RequestParam(required = false) codeName: String?,
        @RequestParam(required = false) guName: String?,
        @PageableDefault(size = 10, sort = ["id"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<Page<EventListResponse>> =
        ResponseEntity.ok(eventService.searchEvents(title, codeName, guName, pageable))

    @GetMapping("/{eventId}")
    fun getEventDetail(@PathVariable eventId: Long): ResponseEntity<EventDetailResponse> =
        ResponseEntity.ok(eventService.getEventDetail(eventId))

    @GetMapping("/search")
    fun search(
        @RequestParam(required = false) title: String?,
        @RequestParam(required = false) codeName: String?,
        @RequestParam(required = false) guName: String?,
        @PageableDefault(size = 10, sort = ["id"], direction = Sort.Direction.DESC) pageable: Pageable
    ): ResponseEntity<Page<EventListResponse>> =
        ResponseEntity.ok(eventService.searchEvents(title, codeName, guName, pageable))

    @GetMapping("/nearby")
    fun getNearbyEvents(
        @RequestParam latitude: Double,
        @RequestParam longitude: Double,
        @RequestParam(defaultValue = "1000.0") radiusMeters: Double
    ): ResponseEntity<List<EventListResponse>> =
        ResponseEntity.ok(eventService.findNearbyEvents(latitude, longitude, radiusMeters))
}
