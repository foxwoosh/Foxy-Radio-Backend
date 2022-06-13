package studio.foxwoosh.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import studio.foxwoosh.AppJson
import studio.foxwoosh.http_responses.LyricsResponse

fun Application.configureRouting() {
    routing {
        get("/lyrics") {
            call.respondText {
                val artist = call.request.queryParameters["artist"] ?: ""
                val title = call.request.queryParameters["title"] ?: ""

                AppJson.encodeToString(LyricsResponse("no lyrics available"))
            }
        }
    }
}