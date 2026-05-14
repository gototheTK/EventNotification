package org.service.event.domain.event

import java.time.LocalDateTime

data class EventListResponse(
    val id: Long,
    val title: String,
    val posterUrl: String?,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val placeName: String?,
    val isFree: String?,
    val promotedTier: PromotedTier
) {
    constructor(event: Event) : this(
        id = event.id,
        title = event.title,
        posterUrl = event.posterUrl,
        startDate = event.period.startDate,
        endDate = event.period.endDate,
        placeName = event.location.placeName,
        isFree = event.usageInfo.isFree,
        promotedTier = event.promotedTier
    )
}

data class EventDetailResponse(
    val id: Long,
    val title: String,
    val posterUrl: String?,
    val promotedTier: PromotedTier,

    val codeName: String?,
    val themeCode: String?,

    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val dateText: String?,
    val proTime: String?,

    val placeName: String?,
    val guName: String?,
    val latitude: Double?,
    val longitude: Double?,

    val isFree: String?,
    val useFee: String?,
    val useTarget: String?,

    val player: String?,
    val program: String?,
    val etcDesc: String?,

    val orgName: String?,
    val inquiry: String?,
    val orgLink: String?,
    val hmpgAddr: String?
) {
    constructor(event: Event) : this(
        id = event.id,
        title = event.title,
        posterUrl = event.posterUrl,
        promotedTier = event.promotedTier,
        codeName = event.category.codeName,
        themeCode = event.category.themeCode,
        startDate = event.period.startDate,
        endDate = event.period.endDate,
        dateText = event.period.dateText,
        proTime = event.period.proTime,
        placeName = event.location.placeName,
        guName = event.location.guName,
        // JTS Point: x = 경도(longitude), y = 위도(latitude)
        latitude = event.location.locationPoint?.y,
        longitude = event.location.locationPoint?.x,
        isFree = event.usageInfo.isFree,
        useFee = event.usageInfo.useFee,
        useTarget = event.usageInfo.useTarget,
        player = event.detail.player,
        program = event.detail.program,
        etcDesc = event.detail.etcDesc,
        orgName = event.organization.orgName,
        inquiry = event.organization.inquiry,
        orgLink = event.organization.orgLink,
        hmpgAddr = event.organization.hmpgAddr
    )
}
