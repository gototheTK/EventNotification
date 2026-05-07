package org.example.board.domain

import org.example.board.entity.Event
import org.springframework.data.jpa.domain.Specification
import jakarta.persistence.criteria.Predicate

class EventSpecification {
    companion object {
        fun searchWith(title: String?, codeName: String?, guName: String?): Specification<Event> {
            return Specification { root, _, cb ->
                val predicates = mutableListOf<Predicate>()

                // 1. 제목 검색 (Null & 빈 문자열 동시에 방어)
                title?.takeIf { it.isNotBlank() }?.let {
                    predicates.add(cb.like(root.get("title"), "%$it%"))
                }

                // 2. 카테고리 검색
                codeName?.takeIf { it.isNotBlank() }?.let {
                    predicates.add(cb.equal(root.get<Any>("category").get<String>("codeName"), it))
                }

                // 3. 지역구 검색
                guName?.takeIf { it.isNotBlank() }?.let {
                    predicates.add(cb.equal(root.get<Any>("location").get<String>("guName"), it))
                }

                // 💡 핵심 최적화: 검색 조건이 하나도 없다면 null을 반환하여 전체 검색 수행
                if (predicates.isEmpty()) {
                    null
                } else {
                    cb.and(*predicates.toTypedArray())
                }
            }
        }
    }
}