package org.example.board.service

import org.example.board.entity.EventLike
import org.example.board.exception.DuplicateLikeException
import org.example.board.exception.EventNotFoundException
import org.example.board.exception.LikeNotFoundException
import org.example.board.exception.MemberNotFoundException
import org.example.board.repository.EventLikeRepository
import org.example.board.repository.EventRepository
import org.example.board.repository.MemberRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true) // 기본적으로 읽기 전용으로 설정
class EventLikeService(
    private val eventRepository: EventRepository,
    private val memberRepository: MemberRepository,
    private val eventLikeRepository: EventLikeRepository
) {

    // 💡 찜하기 (상태를 변경하므로 @Transactional 오버라이드)
    @Transactional
    fun addLike(eventId: Long, email: String) {
        // 1. 유저와 행사 조회 (없으면 바로 우리가 만든 예외를 던짐!)
        val member = memberRepository.findByEmail(email) ?: throw MemberNotFoundException()
        val event = eventRepository.findByIdOrNull(eventId) ?: throw EventNotFoundException()

        // 2. 이미 찜했는지 검증
        if (eventLikeRepository.existsByMemberIdAndEventId(member.id, event.id)) {
            throw DuplicateLikeException()
        }

        // 3. 찜하기 저장
        val eventLike = EventLike(member = member, event = event)
        eventLikeRepository.save(eventLike)
    }

    // 💡 찜 취소하기
    @Transactional
    fun removeLike(eventId: Long, email: String) {
        val member = memberRepository.findByEmail(email) ?: throw MemberNotFoundException()

        // 찜한 기록이 있는지 찾고, 없으면 예외 발생
        val eventLike = eventLikeRepository.findByMemberIdAndEventId(member.id, eventId)
            ?: throw LikeNotFoundException()

        // 기록 삭제
        eventLikeRepository.delete(eventLike)
    }
}