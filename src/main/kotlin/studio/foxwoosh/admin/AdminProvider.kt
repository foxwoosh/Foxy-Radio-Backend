package studio.foxwoosh.admin

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import studio.foxwoosh.ultra.Connection

fun Application.installAdminProvider(connections: Set<Connection>) {
    routing {
        get("/admin") {
            call.respondText {
                val key = call.request.queryParameters["k"]

                if (key != System.getenv("SECRET_ADMIN_KEY")) return@respondText "Fuck you :)"
                val sb = StringBuilder()

                sb.append("Current connections - ${connections.size}\n\n")
                var index = 1
                connections.forEach {
                    sb.append("${index++}:\n")
                    sb.append(it.toString())
                    sb.append("\n\n")
                }

                sb.toString()
            }
        }
    }
}