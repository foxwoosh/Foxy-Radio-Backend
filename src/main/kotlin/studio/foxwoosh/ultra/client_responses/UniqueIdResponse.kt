package studio.foxwoosh.ultra.client_responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UniqueIdResponse(
    @SerialName("uniqueid") val uniqueID: String
)