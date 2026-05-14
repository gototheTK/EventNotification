package org.service.event.domain.event

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface EventRepository : JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {

    fun existsByTitle(title: String): Boolean

    /**
     * PostGIS ST_DWithin을 사용하여 현재 위치(위도/경도) 기준 반경 N 미터 이내 행사를 조회합니다.
     * geography 캐스팅을 통해 구면 거리 계산(미터 단위)을 보장합니다.
     */
    @Query(
        value = """
            SELECT * FROM event
            WHERE location_point IS NOT NULL
              AND ST_DWithin(
                    location_point::geography,
                    ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326)::geography,
                    :radiusMeters
                  )
        """,
        nativeQuery = true
    )
    fun findEventsWithinRadius(
        @Param("latitude") latitude: Double,
        @Param("longitude") longitude: Double,
        @Param("radiusMeters") radiusMeters: Double
    ): List<Event>
}
