package org.service.event.domain.like

import org.springframework.data.jpa.repository.JpaRepository

interface EventLikeRepository : JpaRepository<EventLike, Long> {

    fun existsByMemberIdAndEventId(memberId: Long, eventId: Long): Boolean

    fun findByMemberIdAndEventId(memberId: Long, eventId: Long): EventLike?

    fun findAllByMemberId(memberId: Long): List<EventLike>
}
