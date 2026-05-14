package org.service.event.domain.like

import jakarta.persistence.*
import org.service.event.domain.event.Event
import org.service.event.domain.member.Member

@Entity
@Table(
    uniqueConstraints = [UniqueConstraint(columnNames = ["member_id", "event_id"])]
)
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
