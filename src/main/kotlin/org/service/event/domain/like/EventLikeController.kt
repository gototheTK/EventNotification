package org.service.event.domain.like

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.User
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/events")
class EventLikeController(
    private val eventLikeService: EventLikeService
) {

    @PostMapping("/{eventId}/likes")
    fun likeEvent(
        @PathVariable eventId: Long,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<Map<String, String>> {
        // user.username = JWT subject = email (JwtTokenProvider.getAuthentication 참고)
        val email = user.username
        eventLikeService.addLike(eventId, email)
        return ResponseEntity.ok(mapOf("message" to "행사를 성공적으로 찜했습니다."))
    }

    @DeleteMapping("/{eventId}/likes")
    fun unlikeEvent(
        @PathVariable eventId: Long,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<Map<String, String>> {
        val email = user.username
        eventLikeService.removeLike(eventId, email)
        return ResponseEntity.ok(mapOf("message" to "찜하기를 취소했습니다."))
    }
}
