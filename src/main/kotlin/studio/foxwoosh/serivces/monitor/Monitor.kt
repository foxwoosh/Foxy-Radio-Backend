package studio.foxwoosh.serivces.monitor

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import studio.foxwoosh.database.UserDao
import studio.foxwoosh.database.tables.UserRole
import studio.foxwoosh.serivces.auth.ValidatedUserPrincipal
import studio.foxwoosh.serivces.socket.SocketConnection

fun Application.monitor(connections: Set<SocketConnection>) {
    routing {
        authenticate {
            get("/monitor") {
                val validatedUser = call.principal<ValidatedUserPrincipal>() ?: run {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@get
                }

                val userRole = UserDao.get(validatedUser.id)?.role ?: run {
                    call.respond(HttpStatusCode.Unauthorized)
                    return@get
                }

                if (userRole == UserRole.ADMIN) {
                    call.respondText {
                        val sb = StringBuilder()

                        sb.append("Current connections - ${connections.size}\n\n")
                        var index = 1
                        connections.forEach {
                            sb.append("${index++}:\n")
                            sb.append(it.toString())
                            sb.append("\n\n")
                        }

                        sb.toString().trim()
                    }
                } else {
                    call.respond(HttpStatusCode.Unauthorized)
                }
            }
        }
    }
}