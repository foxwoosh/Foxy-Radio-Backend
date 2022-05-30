package studio.foxwoosh.ultra.http_responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UniqueIdResponse(
    @SerialName("uniqueid") val uniqueID: String
)