package studio.foxwoosh.ultra.socket_incomes

@kotlinx.serialization.Serializable
data class SocketIncome(
    val type: Type,
    val params: Map<String, String>?
) {
    operator fun get(key: String) = params?.get(key)

    enum class Type {
        SUBSCRIBE, UNSUBSCRIBE
    }
}