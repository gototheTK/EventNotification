package org.service.event.domain.event

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.locationtech.jts.geom.Coordinate
import org.locationtech.jts.geom.GeometryFactory
import org.locationtech.jts.geom.PrecisionModel
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.service.event.global.EventNotFoundException
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class EventServiceTest {

    @Mock
    private lateinit var eventRepository: EventRepository

    @InjectMocks
    private lateinit var eventService: EventService

    private val geometryFactory = GeometryFactory(PrecisionModel(), 4326)

    @Test
    @DisplayName("행사 목록을 페이징하여 정상적으로 조회해야 한다")
    fun getEventListSuccess() {
        val pageable = PageRequest.of(0, 10)
        val dummyEvent = createDummyEvent(1L, "서울 재즈 페스티벌")
        val mockPage = PageImpl(listOf(dummyEvent), pageable, 1)

        whenever(eventRepository.findAll(any<PageRequest>())).thenReturn(mockPage)

        val result = eventService.getEventList(pageable)

        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].title).isEqualTo("서울 재즈 페스티벌")
    }

    @Test
    @DisplayName("존재하는 행사 ID로 상세 조회 시 데이터를 반환해야 한다")
    fun getEventDetailSuccess() {
        val eventId = 1L
        val dummyEvent = createDummyEvent(eventId, "싸이 흠뻑쇼")

        `when`(eventRepository.findById(eventId)).thenReturn(Optional.of(dummyEvent))

        val result = eventService.getEventDetail(eventId)

        assertThat(result.id).isEqualTo(eventId)
        assertThat(result.title).isEqualTo("싸이 흠뻑쇼")
        assertThat(result.isFree).isEqualTo("유료")
    }

    @Test
    @DisplayName("존재하지 않는 행사 ID로 조회 시 EventNotFoundException이 발생해야 한다")
    fun getEventDetailFail() {
        val invalidEventId = 999L
        `when`(eventRepository.findById(invalidEventId)).thenReturn(Optional.empty())

        assertThrows<EventNotFoundException> {
            eventService.getEventDetail(invalidEventId)
        }
    }

    @Test
    @DisplayName("검색 조건(제목)이 주어지면 필터링된 목록을 반환해야 한다")
    fun searchEventWithTitle() {
        val pageable = PageRequest.of(0, 10)
        val dummyEvent = createDummyEvent(1L, "서울 재즈 페스티벌")
        val mockPage = PageImpl(listOf(dummyEvent), pageable, 1)

        whenever(eventRepository.findAll(any<Specification<Event>>(), any<Pageable>()))
            .thenReturn(mockPage)

        val result = eventService.searchEvents("페스티벌", null, null, pageable)

        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].title).contains("페스티벌")
    }

    private fun createDummyEvent(id: Long, title: String): Event {
        val point = geometryFactory.createPoint(Coordinate(127.1, 37.5))
        return Event(
            id = id, title = title, posterUrl = "http://test.com/img.jpg",
            category = EventCategory("콘서트", "음악"),
            period = EventPeriod(LocalDateTime.now(), LocalDateTime.now().plusDays(3), "2024-05-01~05-03", "18:00"),
            location = EventLocation("올림픽공원", "송파구", point),
            usageInfo = EventUsageInfo("유료", "110,000원", "누구나"),
            detail = EventDetail("싸이", "물 뿌리는 콘서트", "우비 제공"),
            organization = EventOrganization("기획사", "02-123-4567", "http://org.com", "http://hmpg.com", "시민", "2024-04-01")
        )
    }
}
