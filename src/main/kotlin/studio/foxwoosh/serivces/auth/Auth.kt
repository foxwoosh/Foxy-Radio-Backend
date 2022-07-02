package studio.foxwoosh.serivces.auth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import studio.foxwoosh.database.UserDao
import studio.foxwoosh.database.tables.UserRole
import studio.foxwoosh.http_responses.UserResponse

fun Application.auth() {
    install(Authentication) {
        jwt {
            verifier(AuthConfig.verifier)
            validate { credential ->
                credential.payload.getClaim("id").asLong()?.let { ValidatedUserPrincipal(it) }
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }

    routing {
        post("/v1/login") {
            val user = call.receive<AuthUserRequest>()

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
                    return@post
                }
            }

            call.respond(HttpStatusCode.Forbidden, "Wrong password or user does not exist")
        }

        post("/v1/register") {
            val user = call.receive<RegisterUserRequest>()

            if (UserDao.get(user.login) != null) {
                call.respond(HttpStatusCode.Conflict, "This username already registered")
                return@post
            }

            val id = UserDao.save(
                login = user.login,
                password = user.password,
                name = user.name,
                email = user.email,
                role = UserRole.USER
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

data class ValidatedUserPrincipal(val id: Long): Principal

@Serializable
data class AuthUserRequest(val login: String, val password: String)

@Serializable
data class RegisterUserRequest(val login: String, val password: String, val email: String = "", val name: String = "")