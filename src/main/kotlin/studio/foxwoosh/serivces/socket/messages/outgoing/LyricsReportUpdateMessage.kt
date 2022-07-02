package studio.foxwoosh.serivces.socket.messages.outgoing

import kotlinx.serialization.SerialName
import studio.foxwoosh.database.tables.LyricsReportState
import studio.foxwoosh.serivces.socket.messages.MessageType

@kotlinx.serialization.Serializable
data class LyricsReportUpdateMessage(
    @SerialName("type") val type: MessageType,
    @SerialName("report_id") val reportID: String,
    @SerialName("author_id") val authorID: Long,
    @SerialName("lyrics_id") val lyricsID: String,
    @SerialName("state") val state: LyricsReportState,
    @SerialName("moderator_id") val moderatorID: Long?,
    @SerialName("moderator_comment") val moderatorComment: String?
)