package studio.foxwoosh.ultra.socket_outcomes

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class UltraSocketOutcome<T>(
    @SerialName("t") val type: UltraSocketOutcomeType,
    @SerialName("d") val data: T
)