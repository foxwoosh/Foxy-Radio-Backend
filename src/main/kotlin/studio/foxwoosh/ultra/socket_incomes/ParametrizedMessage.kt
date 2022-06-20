package studio.foxwoosh.ultra.socket_incomes

import kotlinx.serialization.Serializable

@Serializable
data class ParametrizedMessage(
    val type: Type,
    val params: Map<String, String>
) {
    operator fun get(key: String) = params[key]

    enum class Type {
        SUBSCRIBE, UNSUBSCRIBE
    }
}