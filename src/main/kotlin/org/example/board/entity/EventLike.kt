package org.example.board.entity

import jakarta.persistence.*

@Entity
// 💡 한 회원이 같은 행사를 두 번 찜하는 것을 DB 레벨에서 완벽히 차단하는 제약조건!
@Table(uniqueConstraints = [
    UniqueConstraint(columnNames = ["member_id", "event_id"])
])
class EventLike(

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    val event: Event,

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
)