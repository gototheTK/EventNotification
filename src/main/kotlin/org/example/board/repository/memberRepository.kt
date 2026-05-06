package org.example.board.repository

import org.example.board.entity.Member
import org.springframework.data.jpa.repository.JpaRepository

interface MemberRepository: JpaRepository<Member, Long> {

    fun findByEmail(email: String): Member?

}