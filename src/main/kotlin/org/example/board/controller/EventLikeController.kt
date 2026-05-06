package org.example.board.controller

import org.example.board.service.EventLikeService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.User
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/events")
class EventLikeController(
    private val eventLikeService: EventLikeService
) {

    // 💡 1. 행사 찜하기 (POST /api/events/{eventId}/likes)
    @PostMapping("/{eventId}/likes")
    fun likeEvent(
        @PathVariable eventId: Long,
        // 시큐리티 컨텍스트에서 유저 정보를 바로 가져옵니다. (우리가 Token의 Subject에 이메일을 넣었음)
        @AuthenticationPrincipal user: User
    ): ResponseEntity<Map<String, String>> {

        eventLikeService.addLike(eventId, user.username) // user.username에 이메일이 들어있음

        return ResponseEntity.ok(mapOf("message" to "행사를 성공적으로 찜했습니다."))
    }

    // 💡 2. 행사 찜 취소하기 (DELETE /api/events/{eventId}/likes)
    @DeleteMapping("/{eventId}/likes")
    fun unlikeEvent(
        @PathVariable eventId: Long,
        @AuthenticationPrincipal user: User
    ): ResponseEntity<Map<String, String>> {

        eventLikeService.removeLike(eventId, user.username)

        return ResponseEntity.ok(mapOf("message" to "찜하기를 취소했습니다."))
    }
}