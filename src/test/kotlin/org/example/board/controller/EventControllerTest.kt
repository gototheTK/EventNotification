package org.example.board.controller

// 기존 import 문들 (MockMvcRequestBuilders, MockMvcResultMatchers 등)은 유지

// 💡 기존 MockBean 대신 새로운 MockitoBean 패키지를 임포트합니다.
import org.example.board.dto.EventDetailResponse
import org.example.board.dto.EventListResponse
import org.example.board.filter.JwtAuthenticationFilter
import org.example.board.provider.JwtTokenProvider
import org.example.board.service.EventService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull

@WebMvcTest(EventController::class)
@AutoConfigureMockMvc(addFilters = false)
class EventControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    // 💡 @MockBean -> @MockitoBean으로 변경되었습니다.
    @MockitoBean
    private lateinit var eventService: EventService

    // 💡 SecurityConfig가 찾고 있는 두 개의 빈을 가짜로 띄워줍니다!
    @MockitoBean
    private lateinit var jwtAuthenticationFilter: JwtAuthenticationFilter

    @MockitoBean
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @Test
    @DisplayName("GET /api/events 요청 시 상태코드 200과 페이징된 목록을 반환한다")
    fun getEventsApiSuccess() {
        // given
        val dummyResponse = EventListResponse(
            id = 1L, title = "어린이날 축제", posterUrl = null,
            startDate = LocalDateTime.now(), endDate = LocalDateTime.now().plusDays(1),
            placeName = "시청 광장", isFree = "무료"
        )
        val mockPage = PageImpl(listOf(dummyResponse), PageRequest.of(0, 10), 1)

        // 💡 핵심 수정: getEventList가 아니라 searchEvents를 모킹(Mocking)합니다.
        // 검색어(title, codeName, guName)는 null이 들어올 수 있으므로 anyOrNull()을 사용합니다.
        // 💡 anyOrNull 뒤에 <String>을 붙여서 타입을 명확히 알려줍니다!
        whenever(
            eventService.searchEvents(
                anyOrNull<String>(),
                anyOrNull<String>(),
                anyOrNull<String>(),
                any() // Pageable은 보통 잘 유추하지만, 혹시 여기도 에러가 나면 any<Pageable>() 로 적어주세요.
            )
        ).thenReturn(mockPage)

        // when & then
        mockMvc.perform(
            get("/api/events")
                .param("page", "0")
                .param("size", "10")
                // 💡 필요하다면 .param("title", "축제") 처럼 검색어 파라미터를 추가해 테스트할 수도 있습니다.
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content[0].id").value(1L))
            .andExpect(jsonPath("$.content[0].title").value("어린이날 축제"))
    }

    @Test
    @DisplayName("GET /api/events/{eventId} 요청 시 상태코드 200과 상세 정보를 반환한다")
    fun getEventDetailApiSuccess() {
        // given
        val eventId = 1L
        val dummyDetail = EventDetailResponse(
            id = eventId, title = "봄꽃 축제", posterUrl = null,
            codeName = "축제", themeCode = "자연",
            startDate = LocalDateTime.now(), endDate = LocalDateTime.now().plusDays(7),
            dateText = null, proTime = null,
            placeName = "여의도", guName = "영등포구", latitude = 0.0, longitude = 0.0,
            isFree = "무료", useFee = null, useTarget = "모두",
            player = null, program = null, etcDesc = null,
            orgName = "서울시", inquiry = null, orgLink = null, hmpgAddr = null
        )

        // 💡 핵심 수정 2: `when` 대신 whenever 사용
        whenever(eventService.getEventDetail(eventId)).thenReturn(dummyDetail)

        // when & then
        mockMvc.perform(
            get("/api/events/{eventId}", eventId)
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(eventId))
            .andExpect(jsonPath("$.title").value("봄꽃 축제"))
            .andExpect(jsonPath("$.placeName").value("여의도"))
    }
}