package org.service.event.domain.like

import org.service.event.domain.event.EventRepository
import org.service.event.domain.member.MemberRepository
import org.service.event.global.DuplicateLikeException
import org.service.event.global.EventNotFoundException
import org.service.event.global.LikeNotFoundException
import org.service.event.global.MemberNotFoundException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class EventLikeService(
    private val eventRepository: EventRepository,
    private val memberRepository: MemberRepository,
    private val eventLikeRepository: EventLikeRepository
) {

    @Transactional
    fun addLike(eventId: Long, email: String) {
        val member = memberRepository.findByEmail(email) ?: throw MemberNotFoundException()
        val event = eventRepository.findByIdOrNull(eventId) ?: throw EventNotFoundException()

        // 빠른 경로: 이미 찜한 경우 DB 왕복 없이 즉시 거부
        if (eventLikeRepository.existsByMemberIdAndEventId(member.id, event.id)) {
            throw DuplicateLikeException()
        }

        // saveAndFlush()로 즉시 플러시하여 try-catch 범위 안에서 DB 제약 위반을 포착한다.
        // save()를 사용하면 플러시가 트랜잭션 커밋 시점으로 지연되어 catch가 동작하지 않는다.
        try {
            eventLikeRepository.saveAndFlush(EventLike(member = member, event = event))
        } catch (e: DataIntegrityViolationException) {
            // existsByMemberIdAndEventId 통과 후 동시 요청이 유니크 제약을 위반한 경우
            throw DuplicateLikeException()
        }
    }

    @Transactional
    fun removeLike(eventId: Long, email: String) {
        val member = memberRepository.findByEmail(email) ?: throw MemberNotFoundException()
        val eventLike = eventLikeRepository.findByMemberIdAndEventId(member.id, eventId)
            ?: throw LikeNotFoundException()
        eventLikeRepository.delete(eventLike)
    }
}
