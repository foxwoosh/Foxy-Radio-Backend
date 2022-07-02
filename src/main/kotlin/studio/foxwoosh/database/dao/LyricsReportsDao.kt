package studio.foxwoosh.database.dao

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import studio.foxwoosh.database.AppDatabase
import studio.foxwoosh.database.LyricsReportDispatcher
import studio.foxwoosh.database.tables.LyricsReport
import studio.foxwoosh.database.tables.LyricsReportState
import studio.foxwoosh.database.tables.LyricsReports
import java.util.UUID

interface ILyricsReportsDao {

    suspend fun get(reportID: String): LyricsReport?
    suspend fun getUserReports(userID: Long): List<LyricsReport>
    suspend fun saveReport(
        authorID: Long,
        lyricsID: String,
        userComment: String,
        state: LyricsReportState
    ): LyricsReport

    suspend fun updateReportState(
        id: String,
        state: LyricsReportState,
        moderatorID: Long,
        moderatorComment: String
    ): Boolean
}

class LyricsReportsDao : ILyricsReportsDao {

    override suspend fun get(reportID: String) = AppDatabase.query(LyricsReportDispatcher) {
        LyricsReports
            .select { LyricsReports.id eq reportID }
            .map { get(it) }
            .singleOrNull()
    }

    override suspend fun getUserReports(userID: Long) = AppDatabase.query(LyricsReportDispatcher) {
        LyricsReports
            .select { LyricsReports.reportAuthorID eq userID }
            .map { get(it) }
    }

    override suspend fun saveReport(
        authorID: Long,
        lyricsID: String,
        userComment: String,
        state: LyricsReportState
    ): LyricsReport {
        val id = UUID.randomUUID().toString()

        AppDatabase.query(LyricsReportDispatcher) {
            LyricsReports.insert {
                it[LyricsReports.id] = id
                it[LyricsReports.reportAuthorID] = authorID
                it[LyricsReports.lyricsID] = lyricsID
                it[LyricsReports.userComment] = userComment
                it[LyricsReports.state] = state.name
            }
        }

        return LyricsReport(
            id = id,
            reportAuthorID = authorID,
            lyricsID = lyricsID,
            userComment = userComment,
            state = state,
            moderatorID = null,
            moderatorComment = null
        )
    }

    override suspend fun updateReportState(
        id: String,
        state: LyricsReportState,
        moderatorID: Long,
        moderatorComment: String
    ): Boolean =
        AppDatabase.query(LyricsReportDispatcher) {
            LyricsReports.update({ LyricsReports.id eq id }) {
                it[LyricsReports.state] = state.name
                it[LyricsReports.moderatorID] = moderatorID
                it[LyricsReports.moderatorComment] = moderatorComment
            } > 0
        }

    private fun get(row: ResultRow): LyricsReport {
        return LyricsReport(
            id = row[LyricsReports.id],
            reportAuthorID = row[LyricsReports.reportAuthorID],
            lyricsID = row[LyricsReports.lyricsID],
            userComment = row[LyricsReports.userComment],
            state = LyricsReportState.valueOf(row[LyricsReports.state]),
            moderatorID = row[LyricsReports.moderatorID],
            moderatorComment = row[LyricsReports.moderatorComment]
        )
    }
}