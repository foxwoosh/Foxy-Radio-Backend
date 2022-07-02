package studio.foxwoosh.database.tables

import org.jetbrains.exposed.sql.Table

data class LyricsReport(
    val id: String,
    val userID: Long,
    val lyricsID: String,
    val comment: String,
    val state: LyricsReportState
)

object LyricsReports : Table() {
    val id = text("id")
    val userID = long("user_id")
    val lyricsID = text("lyrics_id")
    val comment = text("comment")
    val state = text("state")

    override val primaryKey = PrimaryKey(id)
}

enum class LyricsReportState {
    SUBMITTED, DECLINED, SOLVED
}