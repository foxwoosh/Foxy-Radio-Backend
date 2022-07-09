package studio.foxwoosh.database.tables

import org.jetbrains.exposed.sql.Table

data class LyricsReport(
    val id: String,
    val reportAuthorID: Long,
    val lyricsID: Int,
    val userComment: String,
    val state: LyricsReportState,
    val moderatorID: Long?,
    val moderatorComment: String?
)

object LyricsReports : Table(name = "lyrics_reports") {
    val id = text("id")
    val reportAuthorID = long("author_id")
    val lyricsID = integer("lyrics_id")
    val userComment = text("user_comment")
    val state = text("state")
    val moderatorID = long("moderator_id").nullable()
    val moderatorComment = text("moderator_comment").nullable()

    override val primaryKey = PrimaryKey(id)
}

enum class LyricsReportState {
    SUBMITTED, DECLINED, SOLVED
}