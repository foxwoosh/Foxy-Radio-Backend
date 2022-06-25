package studio.foxwoosh.serivces.lyrics

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*

fun Application.lyricsReports() {
    routing {
        authenticate {
            put("/v1/lyrics/report") {

            }
        }
    }
}