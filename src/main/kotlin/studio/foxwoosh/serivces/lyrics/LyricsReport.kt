package studio.foxwoosh.serivces.lyrics

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.SerialName
import studio.foxwoosh.database.LyricsReportsDao
import studio.foxwoosh.database.tables.LyricsReportState
import studio.foxwoosh.serivces.auth.ValidatedUserPrincipal

fun Application.lyricsReports() {
    routing {
        authenticate {
            put("/v1/lyrics/report") {
                val userID = call.authentication.principal<ValidatedUserPrincipal>()?.id ?: run {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@put
                }
                val reportRequest = call.receive<LyricsReportRequest>()

                val reportDb = LyricsReportsDao.saveReport(
                    userID,
                    reportRequest.lyricsID,
                    reportRequest.comment,
                    LyricsReportState.SUBMITTED
                )

                call.respond(
                    HttpStatusCode.OK,
                    LyricsReportResponse(
                        reportDb.id,
                        reportDb.userID,
                        reportDb.lyricsID,
                        reportDb.comment,
                        reportDb.state
                    )
                )
            }
        }
    }
}

@kotlinx.serialization.Serializable
data class LyricsReportRequest(
    @SerialName("lyrics_id") val lyricsID: String,
    @SerialName("comment") val comment: String
)

@kotlinx.serialization.Serializable
data class LyricsReportResponse(
    @SerialName("id") val id: String,
    @SerialName("user_id") val userID: Long,
    @SerialName("lyrics_id") val lyricsID: String,
    @SerialName("comment") val comment: String,
    @SerialName("state") val state: LyricsReportState
)