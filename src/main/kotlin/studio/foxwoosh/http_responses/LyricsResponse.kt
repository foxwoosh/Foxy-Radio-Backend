package studio.foxwoosh.http_responses

@kotlinx.serialization.Serializable
data class LyricsResponse(val lyrics: String) {
    companion object {
        val empty = LyricsResponse("")
    }
}