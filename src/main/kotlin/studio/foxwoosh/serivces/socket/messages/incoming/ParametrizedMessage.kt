package studio.foxwoosh.serivces.socket.messages.incoming

import kotlinx.serialization.Serializable

@Serializable
data class ParametrizedMessage(
    val type: Type,
    val params: Map<String, String>
) {
    operator fun get(key: String) = params[key]

    enum class Type {
        SUBSCRIBE,
        UNSUBSCRIBE,
        LOGGED_USER_DATA,
        USER_LOGOUT
    }
}