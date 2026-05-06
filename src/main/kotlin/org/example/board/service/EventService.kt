package org.example.board.service

import org.example.board.domain.EventSpecification
import org.example.board.dto.EventDetailResponse
import org.example.board.dto.EventListResponse
import org.example.board.exception.EventNotFoundException
import org.example.board.repository.EventRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true) // 단순 조회용 서비스이므로 성능 최적화를 위해 readOnly 적용
class EventService(
    private val eventRepository: EventRepository
) {

    // 💡 1. 행사 목록 조회 (페이징 적용)
    fun getEventList(pageable: Pageable): Page<EventListResponse> {
        // DB에서 Page<Event>를 꺼내온 뒤, 내부의 데이터를 Page<EventListResponse>로 변환(.map)합니다.
        return eventRepository.findAll(pageable)
            .map { event -> EventListResponse(event) }
    }

    // 💡 2. 행사 상세 조회
    fun getEventDetail(eventId: Long): EventDetailResponse {
        val event = eventRepository.findByIdOrNull(eventId)
            ?: throw EventNotFoundException() // 아까 만든 맞춤형 예외 던지기!

        return EventDetailResponse(event)
    }

    fun searchEvents(
        title: String?,
        codeName: String?,
        guName: String?,
        pageable: Pageable
    ): Page<EventListResponse> {
        val spec = EventSpecification.searchWith(title, codeName, guName)

        return eventRepository.findAll(spec, pageable)
            .map { EventListResponse(it) }
    }

}