package studio.foxwoosh.lyrics_services.client_responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AzLyricsResultsResponse(
    @SerialName("songs") val songs: List<Song>,
    @SerialName("term") val term: String,
) {
    @Serializable
    data class Song(
        @SerialName("url") val url: String,
        @SerialName("autocomplete") val autocomplete: String
    )
}