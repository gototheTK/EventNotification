package org.service.event.domain.like

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface EventLikeRepository : JpaRepository<EventLike, Long> {

    fun existsByMemberIdAndEventId(memberId: Long, eventId: Long): Boolean

    fun findByMemberIdAndEventId(memberId: Long, eventId: Long): EventLike?

    // JOIN FETCH로 EventLike와 Event를 한 번의 쿼리로 조회하여 N+1 문제를 방지한다.
    @Query("SELECT el FROM EventLike el JOIN FETCH el.event WHERE el.member.id = :memberId")
    fun findAllByMemberId(@Param("memberId") memberId: Long): List<EventLike>
}
