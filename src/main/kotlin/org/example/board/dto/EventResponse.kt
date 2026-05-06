package org.example.board.dto

import org.example.board.entity.Event
import org.springframework.data.domain.Page
import java.time.LocalDateTime

// 1. 목록 조회용 DTO (가볍게!)
data class EventListResponse(
    val id: Long,
    val title: String,
    val posterUrl: String?,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val placeName: String?,
    val isFree: String?
) {
    // 💡 Entity를 DTO로 쉽게 변환해주는 보조 생성자 (실무 꿀팁!)
    constructor(event: Event) : this(
        id = event.id,
        title = event.title,
        posterUrl = event.posterUrl,
        startDate = event.period.startDate,
        endDate = event.period.endDate,
        placeName = event.location.placeName,
        isFree = event.usageInfo.isFree
    )
}

// 2. 상세 조회용 DTO (모든 정보 포함!)
data class EventDetailResponse(
    val id: Long,
    val title: String,
    val posterUrl: String?,

    // 카테고리
    val codeName: String?,
    val themeCode: String?,

    // 기간 및 시간
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val dateText: String?,
    val proTime: String?,

    // 장소 및 좌표
    val placeName: String?,
    val guName: String?,
    val latitude: Double?,
    val longitude: Double?,

    // 이용 정보
    val isFree: String?,
    val useFee: String?,
    val useTarget: String?,

    // 상세 설명
    val player: String?,
    val program: String?,
    val etcDesc: String?,

    // 주최 및 링크
    val orgName: String?,
    val inquiry: String?,
    val orgLink: String?,
    val hmpgAddr: String?
) {
    constructor(event: Event) : this(
        id = event.id,
        title = event.title,
        posterUrl = event.posterUrl,
        codeName = event.category.codeName, themeCode = event.category.themeCode,
        startDate = event.period.startDate, endDate = event.period.endDate, dateText = event.period.dateText, proTime = event.period.proTime,
        placeName = event.location.placeName, guName = event.location.guName, latitude = event.location.latitude, longitude = event.location.longitude,
        isFree = event.usageInfo.isFree, useFee = event.usageInfo.useFee, useTarget = event.usageInfo.useTarget,
        player = event.detail.player, program = event.detail.program, etcDesc = event.detail.etcDesc,
        orgName = event.organization.orgName, inquiry = event.organization.inquiry, orgLink = event.organization.orgLink, hmpgAddr = event.organization.hmpgAddr
    )
}