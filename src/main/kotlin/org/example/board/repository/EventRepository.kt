package org.example.board.repository

import org.example.board.entity.Event
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor

interface EventRepository: JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    fun existsByTitle(title: String): Boolean

}