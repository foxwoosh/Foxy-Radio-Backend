package studio.foxwoosh.serivces.lyrics

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.encodeToString
import studio.foxwoosh.ClientsConnections
import studio.foxwoosh.database.LyricsReportsDao
import studio.foxwoosh.database.UserDao
import studio.foxwoosh.database.tables.LyricsReportState
import studio.foxwoosh.database.tables.UserRole
import studio.foxwoosh.serivces.auth.ValidatedUserPrincipal
import studio.foxwoosh.serivces.socket.SocketConnection
import studio.foxwoosh.serivces.socket.messages.MessageType
import studio.foxwoosh.serivces.socket.messages.outgoing.LyricsReportUpdateMessage
import studio.foxwoosh.utils.AppJson
import studio.foxwoosh.utils.sendText

fun Application.lyricsReports() {
    routing {
        authenticate {
            put("/v1/lyrics/report") {
                val userID = call.principal<ValidatedUserPrincipal>()?.id ?: run {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@put
                }
                val newReportRequest = call.receive<NewLyricsReportRequest>()

                val report = LyricsReportsDao.saveReport(
                    authorID = userID,
                    lyricsID = newReportRequest.lyricsID,
                    userComment = newReportRequest.comment,
                    state = LyricsReportState.SUBMITTED
                )

                call.respond(HttpStatusCode.OK)

                ClientsConnections
                    .find { it.userID == report.reportAuthorID }
                    ?.session
                    ?.outgoing
                    ?.sendText(
                        AppJson.encodeToString(
                            LyricsReportUpdateMessage(
                                type = MessageType.REPORT_UPDATE,
                                reportID = report.id,
                                authorID = report.reportAuthorID,
                                lyricsID = report.lyricsID,
                                state = report.state,
                                moderatorID = report.moderatorID,
                                moderatorComment = report.moderatorComment
                            )
                        )
                    )
            }

            patch("/v1/lyrics/report/update") {
                val userID = call.principal<ValidatedUserPrincipal>()?.id ?: run {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@patch
                }

                val user = UserDao.get(userID) ?: run {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@patch
                }

                when (user.role) {
                    UserRole.ADMIN,
                    UserRole.MODERATOR -> {
                        val updateRequest = call.receive<UpdateLyricsReportRequest>()

                        val updated = LyricsReportsDao.updateReportState(
                            id = updateRequest.reportID,
                            state = updateRequest.state,
                            moderatorID = user.id,
                            moderatorComment = updateRequest.moderatorComment
                        )

                        if (updated) {
                            val updatedReport = LyricsReportsDao.get(updateRequest.reportID)

                            if (updatedReport != null) {
                                ClientsConnections
                                    .find { it.userID == updatedReport.reportAuthorID }
                                    ?.session
                                    ?.outgoing
                                    ?.sendText(
                                        AppJson.encodeToString(
                                            LyricsReportUpdateMessage(
                                                type = MessageType.REPORT_UPDATE,
                                                reportID = updatedReport.id,
                                                authorID = updatedReport.reportAuthorID,
                                                lyricsID = updatedReport.lyricsID,
                                                state = updatedReport.state,
                                                moderatorID = updatedReport.moderatorID,
                                                moderatorComment = updatedReport.moderatorComment
                                            )
                                        )
                                    )
                            }

                            call.respond(HttpStatusCode.OK)
                        } else {
                            call.respond(HttpStatusCode.NotModified)
                        }
                    }
                    UserRole.USER -> call.respond(HttpStatusCode.Forbidden)
                }
            }
        }
    }
}

@kotlinx.serialization.Serializable
data class NewLyricsReportRequest(
    @SerialName("lyrics_id") val lyricsID: Int,
    @SerialName("comment") val comment: String
)

@kotlinx.serialization.Serializable
data class UpdateLyricsReportRequest(
    @SerialName("report_id") val reportID: String,
    @SerialName("state") val state: LyricsReportState,
    @SerialName("comment") val moderatorComment: String
)

@kotlinx.serialization.Serializable
data class LyricsReportResponse(
    @SerialName("id") val id: String,
    @SerialName("lyrics_id") val lyricsID: Int,
    @SerialName("user_comment") val userComment: String,
    @SerialName("state") val state: LyricsReportState,
    @SerialName("moderator_id") val moderatorID: Long?,
    @SerialName("moderator_comment") val moderatorComment: String?
)