package org.service.event.domain.event

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.whenever
import org.service.event.global.JwtAuthenticationFilter
import org.service.event.global.JwtTokenProvider
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

@WebMvcTest(EventController::class)
@AutoConfigureMockMvc(addFilters = false)
class EventControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var eventService: EventService

    @MockitoBean
    private lateinit var jwtAuthenticationFilter: JwtAuthenticationFilter

    @MockitoBean
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @Test
    @DisplayName("GET /api/events 요청 시 상태코드 200과 페이징된 목록을 반환한다")
    fun getEventsApiSuccess() {
        val dummyResponse = EventListResponse(
            id = 1L, title = "어린이날 축제", posterUrl = null,
            startDate = LocalDateTime.now(), endDate = LocalDateTime.now().plusDays(1),
            placeName = "시청 광장", isFree = "무료", promotedTier = PromotedTier.NONE
        )
        val mockPage = PageImpl(listOf(dummyResponse), PageRequest.of(0, 10), 1)

        whenever(
            eventService.searchEvents(anyOrNull(), anyOrNull(), anyOrNull(), any())
        ).thenReturn(mockPage)

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
        val eventId = 1L
        val dummyDetail = EventDetailResponse(
            id = eventId, title = "봄꽃 축제", posterUrl = null,
            promotedTier = PromotedTier.NONE,
            codeName = "축제", themeCode = "자연",
            startDate = LocalDateTime.now(), endDate = LocalDateTime.now().plusDays(7),
            dateText = null, proTime = null,
            placeName = "여의도", guName = "영등포구", latitude = 0.0, longitude = 0.0,
            isFree = "무료", useFee = null, useTarget = "모두",
            player = null, program = null, etcDesc = null,
            orgName = "서울시", inquiry = null, orgLink = null, hmpgAddr = null
        )

        whenever(eventService.getEventDetail(eventId)).thenReturn(dummyDetail)

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
