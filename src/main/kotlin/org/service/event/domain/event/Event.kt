package org.service.event.domain.event

import jakarta.persistence.*
import org.locationtech.jts.geom.Point
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

enum class PromotedTier { NONE, BRONZE, SILVER, GOLD }

@Entity
@Table(name = "event")
class Event(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val title: String,

    @Column(length = 1000)
    val posterUrl: String?,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val promotedTier: PromotedTier = PromotedTier.NONE,

    @Embedded val category: EventCategory,
    @Embedded val period: EventPeriod,
    @Embedded val location: EventLocation,
    @Embedded val usageInfo: EventUsageInfo,
    @Embedded val detail: EventDetail,
    @Embedded val organization: EventOrganization
) {
    // 시작·종료 시각 경계를 포함하는 닫힌 구간 [startDate, endDate]
    fun isOngoing(): Boolean {
        val now = LocalDateTime.now()
        return !now.isBefore(period.startDate) && !now.isAfter(period.endDate)
    }

    fun getDDay(): Long {
        val today = LocalDateTime.now().toLocalDate()
        return ChronoUnit.DAYS.between(today, period.startDate.toLocalDate())
    }

    fun hasValidCoordinates(): Boolean {
        val point = location.locationPoint ?: return false
        return point.x != 0.0 && point.y != 0.0
    }

    fun isExpired(): Boolean = LocalDateTime.now().isAfter(period.endDate)

    fun isFreeEvent(): Boolean = usageInfo.isFree == "무료"
}

@Embeddable
class EventCategory(
    val codeName: String?,
    val themeCode: String?
)

@Embeddable
class EventPeriod(
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val dateText: String?,
    val proTime: String?
)

@Embeddable
class EventLocation(
    val placeName: String?,
    val guName: String?,
    @Column(columnDefinition = "geometry(Point,4326)")
    val locationPoint: Point?
)

@Embeddable
class EventUsageInfo(
    val isFree: String?,
    val useFee: String?,
    val useTarget: String?
)

@Embeddable
class EventDetail(
    @Column(columnDefinition = "TEXT") val player: String?,
    @Column(columnDefinition = "TEXT") val program: String?,
    @Column(columnDefinition = "TEXT") val etcDesc: String?
)

@Embeddable
class EventOrganization(
    val orgName: String?,
    val inquiry: String?,
    @Column(length = 1000) val orgLink: String?,
    @Column(length = 1000) val hmpgAddr: String?,
    val ticket: String?,
    val rgstDate: String?
)
