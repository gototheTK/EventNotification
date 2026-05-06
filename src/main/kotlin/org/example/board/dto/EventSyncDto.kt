package org.example.board.dto


import com.fasterxml.jackson.annotation.JsonProperty

// API 전체 응답 껍데기
data class SeoulEventResponse(
    @JsonProperty("culturalEventInfo") val culturalEventInfo: CulturalEventInfo?
)

data class CulturalEventInfo(
    @JsonProperty("row") val row: List<SeoulEventRow>?
)

// 24개의 데이터를 모두 받아내는 로우(Row) DTO
data class SeoulEventRow(
    @JsonProperty("CODENAME") val codeName: String?,
    @JsonProperty("GUNAME") val guName: String?,
    @JsonProperty("TITLE") val title: String, // 필수
    @JsonProperty("DATE") val dateText: String?,
    @JsonProperty("PLACE") val place: String?,
    @JsonProperty("ORG_NAME") val orgName: String?,
    @JsonProperty("USE_TRGT") val useTarget: String?,
    @JsonProperty("USE_FEE") val useFee: String?,
    @JsonProperty("INQUIRY") val inquiry: String?,
    @JsonProperty("PLAYER") val player: String?,
    @JsonProperty("PROGRAM") val program: String?,
    @JsonProperty("ETC_DESC") val etcDesc: String?,
    @JsonProperty("ORG_LINK") val orgLink: String?,
    @JsonProperty("MAIN_IMG") val mainImg: String?,
    @JsonProperty("RGSTDATE") val rgstDate: String?,
    @JsonProperty("TICKET") val ticket: String?,
    @JsonProperty("STRTDATE") val startDate: String, // 필수 (예: "2024-05-01 00:00:00.0")
    @JsonProperty("END_DATE") val endDate: String,   // 필수
    @JsonProperty("THEMECODE") val themeCode: String?,
    @JsonProperty("LOT") val longitude: String?,     // API에서 문자열로 넘어옴
    @JsonProperty("LAT") val latitude: String?,      // API에서 문자열로 넘어옴
    @JsonProperty("IS_FREE") val isFree: String?,
    @JsonProperty("HMPG_ADDR") val hmpgAddr: String?,
    @JsonProperty("PRO_TIME") val proTime: String?
)