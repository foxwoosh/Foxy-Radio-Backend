package studio.foxwoosh.serivces.utils

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.math.BigInteger
import java.security.MessageDigest

fun Application.utils() {
    routing {
        get("/md5") {
            val param = call.request.queryParameters["p"] ?: run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            call.respond(
                HttpStatusCode.OK,
                BigInteger(
                    1,
                    MessageDigest.getInstance("MD5").digest(param.toByteArray())
                ).toString(16).padStart(32, '0')
            )
        }
    }
}