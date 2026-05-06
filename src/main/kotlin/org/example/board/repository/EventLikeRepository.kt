package org.example.board.repository

import org.example.board.entity.EventLike
import org.springframework.data.jpa.repository.JpaRepository

interface EventLikeRepository : JpaRepository<EventLike, Long> {

    // 특정 회원이 특정 행사를 이미 찜했는지 확인 (찜 취소/중복 찜 방지용)
    fun existsByMemberIdAndEventId(memberId: Long, eventId: Long): Boolean

    // 특정 회원이 특정 행사를 찜한 기록 찾기 (찜 취소용)
    fun findByMemberIdAndEventId(memberId: Long, eventId: Long): EventLike?

    // 내가 찜한 모든 행사 목록 보기 (마이페이지용)
    fun findAllByMemberId(memberId: Long): List<EventLike>
}