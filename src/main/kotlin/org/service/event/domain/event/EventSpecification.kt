package org.service.event.domain.event

import jakarta.persistence.criteria.Predicate
import org.springframework.data.jpa.domain.Specification

class EventSpecification {
    companion object {
        private const val LIKE_ESCAPE_CHAR = '\\'

        /**
         * LIKE 패턴에 사용되는 와일드카드 문자(\, %, _)를 이스케이프합니다.
         * 이스케이프 문자 자신(\)을 먼저 처리해야 이중 치환을 막을 수 있습니다.
         */
        private fun escapeLike(value: String): String = value
            .replace("$LIKE_ESCAPE_CHAR", "$LIKE_ESCAPE_CHAR$LIKE_ESCAPE_CHAR")
            .replace("%", "$LIKE_ESCAPE_CHAR%")
            .replace("_", "${LIKE_ESCAPE_CHAR}_")

        fun searchWith(title: String?, codeName: String?, guName: String?): Specification<Event> {
            return Specification { root, _, cb ->
                val predicates = mutableListOf<Predicate>()

                title?.takeIf { it.isNotBlank() }?.let { raw ->
                    val pattern = "%${escapeLike(raw)}%"
                    predicates.add(cb.like(root.get("title"), pattern, LIKE_ESCAPE_CHAR))
                }
                codeName?.takeIf { it.isNotBlank() }?.let {
                    predicates.add(cb.equal(root.get<Any>("category").get<String>("codeName"), it))
                }
                guName?.takeIf { it.isNotBlank() }?.let {
                    predicates.add(cb.equal(root.get<Any>("location").get<String>("guName"), it))
                }

                if (predicates.isEmpty()) null else cb.and(*predicates.toTypedArray())
            }
        }
    }
}
