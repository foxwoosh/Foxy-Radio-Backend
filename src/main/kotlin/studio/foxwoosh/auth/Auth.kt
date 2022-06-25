package studio.foxwoosh.auth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import studio.foxwoosh.database.UserDao
import studio.foxwoosh.http_responses.UserResponse

fun Application.auth() {
    install(Authentication) {
        jwt {
            verifier(AuthConfig.verifier)
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }

    routing {
        post("/v1/login") {
            val user = call.receive<AuthUser>()

            UserDao.get(user.login)?.let {
                if (user.password == it.password) {
                    call.respond(
                        HttpStatusCode.OK,
                        UserResponse(
                            id = it.id,
                            name = it.name,
                            login = it.login,
                            email = it.email,
                            token = AuthConfig.generateToken(it.id, it.login, it.password)
                        )
                    )
                } else {
                    call.respond(HttpStatusCode.Forbidden, "Wrong password or user does not exist")
                }
            } ?: call.respond(HttpStatusCode.Forbidden, "Wrong password or user does not exist")
        }

        post("/v1/register") {
            val user = call.receive<RegisterUser>()

            if (UserDao.get(user.login) != null) {
                call.respond(HttpStatusCode.Conflict, "This username already registered")
                return@post
            }

            val id = UserDao.save(
                login = user.login,
                password = user.password,
                name = user.name,
                email = user.email
            )?.id ?: run {
                call.respond(HttpStatusCode.InternalServerError, "Something wrong")
                return@post
            }

            call.respond(
                HttpStatusCode.OK,
                UserResponse(
                    id = id,
                    name = user.name,
                    login = user.login,
                    email = user.email,
                    token = AuthConfig.generateToken(id, user.login, user.password)
                )
            )
        }
    }
}

@Serializable
data class AuthUser(val login: String, val password: String)

@Serializable
data class RegisterUser(val login: String, val password: String, val email: String = "", val name: String = "")