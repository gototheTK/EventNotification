package org.service.event.domain.event

import org.service.event.global.EventNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class EventService(
    private val eventRepository: EventRepository
) {

    fun getEventList(pageable: Pageable): Page<EventListResponse> =
        eventRepository.findAll(pageable).map { EventListResponse(it) }

    fun getEventDetail(eventId: Long): EventDetailResponse {
        val event = eventRepository.findByIdOrNull(eventId) ?: throw EventNotFoundException()
        return EventDetailResponse(event)
    }

    fun searchEvents(
        title: String?,
        codeName: String?,
        guName: String?,
        pageable: Pageable
    ): Page<EventListResponse> {
        val spec = EventSpecification.searchWith(title, codeName, guName)
        return eventRepository.findAll(spec, pageable).map { EventListResponse(it) }
    }

    fun findNearbyEvents(latitude: Double, longitude: Double, radiusMeters: Double): List<EventListResponse> =
        eventRepository.findEventsWithinRadius(latitude, longitude, radiusMeters)
            .map { EventListResponse(it) }
}
