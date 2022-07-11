package studio.foxwoosh.serivces.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm

object AuthConfig {
    private val issuer = System.getenv("AUTH_ISSUER")
    private val algorithm = Algorithm.HMAC512(System.getenv("SECRET_AUTH_KEY"))

    val verifier: JWTVerifier = JWT
        .require(algorithm)
        .withIssuer(issuer)
        .build()

    fun generateToken(id: Long, login: String, password: String): String = JWT.create()
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withClaim("id", id)
        .withClaim("login", login)
        .withClaim("password", password)
        .sign(algorithm)
}