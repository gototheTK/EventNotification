package org.service.event.domain.like

import org.service.event.domain.event.EventRepository
import org.service.event.domain.member.MemberRepository
import org.service.event.global.DuplicateLikeException
import org.service.event.global.EventNotFoundException
import org.service.event.global.LikeNotFoundException
import org.service.event.global.MemberNotFoundException
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class EventLikeService(
    private val eventRepository: EventRepository,
    private val memberRepository: MemberRepository,
    private val eventLikeRepository: EventLikeRepository,
    private val redisTemplate: StringRedisTemplate
) {
    private fun likeKey(eventId: Long) = "event:$eventId:likes"

    @Transactional
    fun addLike(eventId: Long, email: String) {
        val key = likeKey(eventId)

        // ① Redis 1차 방어: SADD는 원자적으로 동작한다.
        //    이미 Set에 존재하면 0을 반환 → DB 쿼리 없이 즉시 거부
        val added = redisTemplate.opsForSet().add(key, email) ?: 0L
        if (added == 0L) throw DuplicateLikeException()

        try {
            // ② DB 2차 방어: Redis 미스(재시작·데이터 공백) 이후 동일 요청이 들어온 경우 처리
            val member = memberRepository.findByEmail(email) ?: throw MemberNotFoundException()
            val event = eventRepository.findByIdOrNull(eventId) ?: throw EventNotFoundException()

            if (eventLikeRepository.existsByMemberIdAndEventId(member.id, event.id)) {
                throw DuplicateLikeException()
            }

            // saveAndFlush()로 즉시 플러시하여 경쟁 조건의 DB 제약 위반을 catch 범위 안에서 포착
            try {
                eventLikeRepository.saveAndFlush(EventLike(member = member, event = event))
            } catch (e: DataIntegrityViolationException) {
                throw DuplicateLikeException()
            }
        } catch (e: DuplicateLikeException) {
            // DB에 이미 찜 존재 → Redis도 같은 상태여야 하므로 롤백 없이 재전파
            throw e
        } catch (e: Exception) {
            // Member/Event 미존재 등 → DB에 like 없음, 선행 추가된 Redis 항목을 롤백
            redisTemplate.opsForSet().remove(key, email)
            throw e
        }
    }

    @Transactional
    fun removeLike(eventId: Long, email: String) {
        val member = memberRepository.findByEmail(email) ?: throw MemberNotFoundException()
        val eventLike = eventLikeRepository.findByMemberIdAndEventId(member.id, eventId)
            ?: throw LikeNotFoundException()

        // DB 삭제 성공 후 Redis 동기화 (트랜잭션 커밋 전에 호출해도 안전: 삭제 실패 시 롤백됨)
        eventLikeRepository.delete(eventLike)
        redisTemplate.opsForSet().remove(likeKey(eventId), email)
    }
}
