package org.service.event.domain.member

import jakarta.persistence.*

@Entity
class Member(
    @Column(unique = true, nullable = false)
    val email: String,

    @Column(nullable = false)
    var password: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val role: Role = Role.ROLE_USER,

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
)
