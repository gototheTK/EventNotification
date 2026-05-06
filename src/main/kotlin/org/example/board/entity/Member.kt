package org.example.board.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.example.board.domain.Role

// 1. 회원 엔티티
@Entity
class Member(

    @Column(unique = true, nullable = false)
    val email: String,

    // 비밀번호는 암호화(Bcrypt)되어 저장되므로 길이를 넉넉하게 주거나 생략(기본 255)합니다.
    @Column(nullable = false)
    var password: String,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING) // Enum 이름을 DB에 문자열로 그대로 저장
    val role: Role = Role.ROLE_USER,

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
)