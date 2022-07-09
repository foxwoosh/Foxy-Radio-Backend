package studio.foxwoosh.http_responses

import kotlinx.serialization.SerialName
import studio.foxwoosh.database.tables.LyricsReportState

@kotlinx.serialization.Serializable
data class LyricsResponse(
    @SerialName("id") val id: Int,
    @SerialName("lyrics") val lyrics: String,
    @SerialName("report_state") val reportState: LyricsReportState?,
    @SerialName("moderator_comment") val moderatorComment: String?
)