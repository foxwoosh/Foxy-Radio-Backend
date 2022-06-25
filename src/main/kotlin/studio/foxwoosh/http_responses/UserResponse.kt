package studio.foxwoosh.http_responses

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: Long,
    val name: String,
    val login: String,
    val email: String,
    val token: String
)