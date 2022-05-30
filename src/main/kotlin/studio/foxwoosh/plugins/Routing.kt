package studio.foxwoosh.plugins

import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*

fun Application.configureRouting() {

    routing {
        get("/") {
            call.respondText("Foxy Radio WS home EZ")
        }
        get("/cock") {
            call.respondText {
                val size = call.request.queryParameters["size"]?.toInt() ?: 0

                val sb = StringBuilder("8")

                for (i in 0..size) {
                    sb.append("=")
                }

                sb.append("3")

                sb.toString()
            }
        }
        get("/raven") {
            call.respondText("Ну здарова равен, че зыришь блять")
        }
    }
}
