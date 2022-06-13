package studio.foxwoosh.lyrics_services.client_responses

import kotlinx.serialization.Serializable

@Serializable
class MusixMatchLyricsResponse(
    val message: Message
) {
    @Serializable
    data class Message(
        val body: Body
    ) {
        @Serializable
        data class Body(
            val lyrics: Lyrics
        ) {
            @Serializable
            data class Lyrics(
                val lyrics_body: String,
//                val html_tracking_url: String,
//                val instrumental: Int,
//                val lyrics_copyright: String,
//                val lyrics_id: Int,
//                val lyrics_language: String,
//                val pixel_tracking_url: String,
//                val restricted: Int,
//                val script_tracking_url: String,
//                val updated_time: String
            )
        }
    }
}