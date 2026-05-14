package org.service.event.infrastructure.sync

import com.fasterxml.jackson.annotation.JsonProperty

data class SeoulEventResponse(
    @JsonProperty("culturalEventInfo") val culturalEventInfo: CulturalEventInfo?
)

data class CulturalEventInfo(
    @JsonProperty("row") val row: List<SeoulEventRow>?
)

data class SeoulEventRow(
    @JsonProperty("CODENAME")  val codeName: String?,
    @JsonProperty("GUNAME")    val guName: String?,
    @JsonProperty("TITLE")     val title: String,
    @JsonProperty("DATE")      val dateText: String?,
    @JsonProperty("PLACE")     val place: String?,
    @JsonProperty("ORG_NAME")  val orgName: String?,
    @JsonProperty("USE_TRGT")  val useTarget: String?,
    @JsonProperty("USE_FEE")   val useFee: String?,
    @JsonProperty("INQUIRY")   val inquiry: String?,
    @JsonProperty("PLAYER")    val player: String?,
    @JsonProperty("PROGRAM")   val program: String?,
    @JsonProperty("ETC_DESC")  val etcDesc: String?,
    @JsonProperty("ORG_LINK")  val orgLink: String?,
    @JsonProperty("MAIN_IMG")  val mainImg: String?,
    @JsonProperty("RGSTDATE")  val rgstDate: String?,
    @JsonProperty("TICKET")    val ticket: String?,
    @JsonProperty("STRTDATE")  val startDate: String,
    @JsonProperty("END_DATE")  val endDate: String,
    @JsonProperty("THEMECODE") val themeCode: String?,
    @JsonProperty("LOT")       val longitude: String?,
    @JsonProperty("LAT")       val latitude: String?,
    @JsonProperty("IS_FREE")   val isFree: String?,
    @JsonProperty("HMPG_ADDR") val hmpgAddr: String?,
    @JsonProperty("PRO_TIME")  val proTime: String?
)
