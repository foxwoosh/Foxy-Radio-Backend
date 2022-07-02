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
    suspend fun getUserReports(userID: Long): List<LyricsReport>
    suspend fun saveReport(
        userID: Long,
        lyricsID: String,
        comment: String,
        state: LyricsReportState
    ): LyricsReport

    suspend fun updateReportState(id: String, state: LyricsReportState): Boolean
}

class LyricsReportsDao : ILyricsReportsDao {
    override suspend fun getUserReports(userID: Long): List<LyricsReport> {
        return LyricsReports
            .select { LyricsReports.userID eq userID }
            .map { get(it) }
    }

    override suspend fun saveReport(
        userID: Long,
        lyricsID: String,
        comment: String,
        state: LyricsReportState
    ): LyricsReport {
        val id = UUID.randomUUID().toString()

        AppDatabase.query(LyricsReportDispatcher) {
            LyricsReports.insert {
                it[LyricsReports.id] = id
                it[LyricsReports.userID] = userID
                it[LyricsReports.lyricsID] = lyricsID
                it[LyricsReports.comment] = comment
                it[LyricsReports.state] = state.name
            }
        }

        return LyricsReport(
            id = id,
            userID = userID,
            lyricsID = lyricsID,
            comment = comment,
            state = state
        )
    }

    override suspend fun updateReportState(id: String, state: LyricsReportState) =
        AppDatabase.query(LyricsReportDispatcher) {
            LyricsReports.update({ LyricsReports.id eq id }) {
                it[LyricsReports.state] = state.name
            } > 0
        }

    private fun get(row: ResultRow): LyricsReport {
        val id = row[LyricsReports.id]
        val userID = row[LyricsReports.userID]
        val lyricsID = row[LyricsReports.lyricsID]
        val comment = row[LyricsReports.comment]
        val state = LyricsReportState.valueOf(row[LyricsReports.state])

        return LyricsReport(
            id = id,
            userID = userID,
            lyricsID = lyricsID,
            comment = comment,
            state = state
        )
    }
}