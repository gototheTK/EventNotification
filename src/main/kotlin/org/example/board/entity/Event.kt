package org.example.board.entity

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@Entity
class Event(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val title: String,

    @Column(length = 1000)
    val posterUrl: String?,

    @Embedded val category: EventCategory,
    @Embedded val period: EventPeriod,
    @Embedded val location: EventLocation,
    @Embedded val usageInfo: EventUsageInfo,
    @Embedded val detail: EventDetail,
    @Embedded val organization: EventOrganization

) {
    // 1. 행사가 지금 '진행 중'인가요? (프론트엔드 뱃지 표시용: [진행중] / [진행예정])
    fun isOngoing(): Boolean {
        val now = LocalDateTime.now()
        // 시작일이 지났고, 종료일이 아직 안 지났다면 진행 중!
        return now.isAfter(this.period.startDate) && now.isBefore(this.period.endDate)
    }

    // 2. 행사 시작까지 며칠 남았나요? (디데이 계산용: "D-3", "D-Day")
    fun getDDay(): Long {
        val today = LocalDateTime.now().toLocalDate()
        val startDay = this.period.startDate.toLocalDate()
        return ChronoUnit.DAYS.between(today, startDay)
    }

    // 3. 지도에 띄울 수 있는 좌표가 있나요? (프론트엔드 '지도 보기' 버튼 활성화 여부)
    fun hasValidCoordinates(): Boolean {
        val lat = this.location.latitude
        val lon = this.location.longitude
        // 공공데이터 특성상 0.0 이나 null이 들어오는 경우가 많아 이를 방어합니다.
        return lat != null && lon != null && lat > 0.0 && lon > 0.0
    }

    // 4. (기존 메서드 복습) 행사 종료 여부
    fun isExpired(): Boolean {
        return LocalDateTime.now().isAfter(this.period.endDate)
    }

    // 5. (기존 메서드 복습) 무료 행사 여부
    fun isFreeEvent(): Boolean {
        return this.usageInfo.isFree == "무료"
    }
}

// --- 💡 6개의 의미 단위로 쪼갠 Value Object들 --- //

@Embeddable
class EventCategory(
    val codeName: String?,        // 1. CODENAME (분류)
    val themeCode: String?        // 19. THEMECODE (테마분류)
)

@Embeddable
class EventPeriod(
    val startDate: LocalDateTime, // 17. STRTDATE (시작일)
    val endDate: LocalDateTime,   // 18. END_DATE (종료일)
    val dateText: String?,        // 4. DATE (날짜 - 텍스트 형태)
    val proTime: String?          // 24. PRO_TIME (행사시간)
)

@Embeddable
class EventLocation(
    val placeName: String?,       // 5. PLACE (장소)
    val guName: String?,          // 2. GUNAME (자치구)
    val latitude: Double?,        // 21. LAT (위도 - API 명세의 X/Y 주의)
    val longitude: Double?        // 20. LOT (경도)
)

@Embeddable
class EventUsageInfo(
    val isFree: String?,          // 22. IS_FREE (유무료)
    val useFee: String?,          // 8. USE_FEE (이용요금)
    val useTarget: String?        // 7. USE_TRGT (이용대상)
)

@Embeddable
class EventDetail(
    @Column(columnDefinition = "TEXT")
    val player: String?,          // 10. PLAYER (출연자정보)

    @Column(columnDefinition = "TEXT")
    val program: String?,         // 11. PROGRAM (프로그램소개)

    @Column(columnDefinition = "TEXT")
    val etcDesc: String?          // 12. ETC_DESC (기타내용)
)

@Embeddable
class EventOrganization(
    val orgName: String?,         // 6. ORG_NAME (기관명)
    val inquiry: String?,         // 9. INQUIRY (문의)

    @Column(length = 1000)
    val orgLink: String?,         // 13. ORG_LINK (홈페이지 주소)

    @Column(length = 1000)
    val hmpgAddr: String?,        // 23. HMPG_ADDR (문화포털상세URL)

    val ticket: String?,          // 16. TICKET (시민/기관)
    val rgstDate: String?         // 15. RGSTDATE (신청일)
)