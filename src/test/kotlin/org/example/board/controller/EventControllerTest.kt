package org.example.board.controller

import org.example.board.dto.EventDetailResponse
import org.example.board.dto.EventListResponse
import org.example.board.filter.JwtAuthenticationFilter
import org.example.board.provider.JwtTokenProvider
import org.example.board.service.EventService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
// 💡 기존 MockBean 대신 새로운 MockitoBean 패키지를 임포트합니다.
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime

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

        `when`(eventService.getEventList(any(Pageable::class.java))).thenReturn(mockPage)

        // when & then
        mockMvc.perform(
            get("/api/events")
                .param("page", "0")
                .param("size", "10")
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

        `when`(eventService.getEventDetail(eventId)).thenReturn(dummyDetail)

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