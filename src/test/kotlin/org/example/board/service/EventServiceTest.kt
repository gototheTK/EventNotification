package org.example.board.service

import org.assertj.core.api.Assertions.assertThat
import org.example.board.entity.Event
import org.example.board.entity.EventCategory
import org.example.board.entity.EventDetail
import org.example.board.entity.EventLocation
import org.example.board.entity.EventOrganization
import org.example.board.entity.EventPeriod
import org.example.board.entity.EventUsageInfo
import org.example.board.exception.EventNotFoundException
import org.example.board.repository.EventRepository
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class EventServiceTest {

    @Mock
    private lateinit var eventRepository: EventRepository // 가짜 레포지토리

    @InjectMocks
    private lateinit var eventService: EventService // 가짜 레포지토리가 주입된 서비스

    @Test
    @DisplayName("행사 목록을 페이징하여 정상적으로 조회해야 한다")
    fun getEventListSuccess() {
        // given (준비)
        val pageable = PageRequest.of(0, 10)
        val dummyEvent = createDummyEvent(1L, "서울 재즈 페스티벌")

        // Repository는 Entity(Event) 페이지를 반환해야 하므로 PageImpl<Event>로 생성
        val mockPage = PageImpl(listOf(dummyEvent), pageable, 1)

        // 💡 핵심 수정: eventService가 아니라 eventRepository를 stubbing합니다!
        whenever(eventRepository.findAll(any<PageRequest>())).thenReturn(mockPage)

        // when (실행: 실제 서비스 로직이 돌아가면서 Repository를 호출함)
        val result = eventService.getEventList(pageable)

        // then (검증)
        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].title).isEqualTo("서울 재즈 페스티벌")
        assertThat(result.totalElements).isEqualTo(1)
    }

    @Test
    @DisplayName("존재하는 행사 ID로 상세 조회 시 데이터를 반환해야 한다")
    fun getEventDetailSuccess() {
        // given
        val eventId = 1L
        val dummyEvent = createDummyEvent(eventId, "싸이 흠뻑쇼")

        `when`(eventRepository.findById(eventId)).thenReturn(Optional.of(dummyEvent))

        // when
        val result = eventService.getEventDetail(eventId)

        // then
        assertThat(result.id).isEqualTo(eventId)
        assertThat(result.title).isEqualTo("싸이 흠뻑쇼")
        assertThat(result.isFree).isEqualTo("유료")
    }

    @Test
    @DisplayName("존재하지 않는 행사 ID로 조회 시 EventNotFoundException이 발생해야 한다")
    fun getEventDetailFail() {
        // given
        val invalidEventId = 999L
        `when`(eventRepository.findById(invalidEventId)).thenReturn(Optional.empty())

        // when & then (실행과 동시에 예외가 터지는지 검증)
        assertThrows<EventNotFoundException> {
            eventService.getEventDetail(invalidEventId)
        }
    }

    // --- 테스트를 위한 더미 객체 생성 헬퍼 메서드 ---
    private fun createDummyEvent(id: Long, title: String): Event {
        return Event(
            id = id, title = title, posterUrl = "http://test.com/img.jpg",
            category = EventCategory("콘서트", "음악"),
            period = EventPeriod(LocalDateTime.now(), LocalDateTime.now().plusDays(3), "2024-05-01~05-03", "18:00"),
            location = EventLocation("올림픽공원", "송파구", 37.5, 127.1),
            usageInfo = EventUsageInfo("유료", "110,000원", "누구나"),
            detail = EventDetail("싸이", "물 뿌리는 콘서트", "우비 제공"),
            organization = EventOrganization("기획사", "02-123-4567", "http://org.com", "http://hmpg.com", "시민", "2024-04-01")
        )
    }

    @Test
    @DisplayName("검색 조건(제목)이 주어지면 필터링된 목록을 반환해야 한다")
    fun searchEventWithTitle() {
        // given
        val title = "페스티벌"
        val pageable = PageRequest.of(0, 10)
        val dummyEvent = createDummyEvent(1L, "서울 재즈 페스티벌")
        val mockPage = PageImpl(listOf(dummyEvent), pageable, 1)

        // Specification을 사용하는 findAll 호출을 Mocking
        whenever(eventRepository.findAll(any<Specification<Event>>(), any<Pageable>()))
            .thenReturn(mockPage)

        // when
        val result = eventService.searchEvents(title, null, null, pageable)

        // then
        assertThat(result.content).hasSize(1)
        assertThat(result.content[0].title).contains("페스티벌")
    }
}