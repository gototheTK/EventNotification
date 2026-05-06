package org.example.board.domain

import org.example.board.entity.Event
import org.springframework.data.jpa.domain.Specification
import jakarta.persistence.criteria.Predicate

class EventSpecification {
    companion object {
        fun searchWith(title: String?, codeName: String?, guName: String?): Specification<Event> {
            return Specification { root, _, cb ->
                val predicates = mutableListOf<Predicate>()

                // 1. 제목 검색 (Like %title%)
                title?.let {
                    predicates.add(cb.like(root.get<String>("title"), "%$it%"))
                }

                // 2. 카테고리 검색 (Value Object인 category 내부 필드 접근)
                codeName?.let {
                    predicates.add(cb.equal(root.get<Any>("category").get<String>("codeName"), it))
                }

                // 3. 지역구 검색 (Value Object인 location 내부 필드 접근)
                guName?.let {
                    predicates.add(cb.equal(root.get<Any>("location").get<String>("guName"), it))
                }

                cb.and(*predicates.toTypedArray())
            }
        }
    }
}