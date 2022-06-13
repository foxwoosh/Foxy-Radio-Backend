package studio.foxwoosh.ultra.messages

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UltraSongDataMessage(
    @SerialName("type") override val type: UltraMessageType,
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("artist") val artist: String,
    @SerialName("album") val album: String? = null,
    @SerialName("cover") val cover: String,
    @SerialName("date") val date: String,
    @SerialName("time") val time: String,
    @SerialName("root") val root: String,
    @SerialName("metadata") val metadata: String,
    @SerialName("itunes_url") val iTunesUrl: String?,
    @SerialName("spotify_url") val spotifyUrl: String?,
    @SerialName("yamusic_url") val yandexMusicUrl: String?,
    @SerialName("youtube_url") val youtubeUrl: String?,
    @SerialName("ytmusic_url") val youtubeMusicUrl: String?,
    @SerialName("prev_tracks") val previousTracks: List<PreviousTrack>
) : UltraMessage {
    @Serializable
    data class PreviousTrack(
        @SerialName("title") val title: String,
        @SerialName("artist") val artist: String,
        @SerialName("cover") val cover: String,
        @SerialName("date") val date: String,
        @SerialName("time") val time: String,
        @SerialName("itunes_url") val itunesUrl: String?,
        @SerialName("spotify_url") val spotifyUrl: String?,
        @SerialName("yamusic_url") val yandexMusicUrl: String?,
        @SerialName("youtube_url") val youtubeUrl: String?,
        @SerialName("ytmusic_url") val youtubeMusicUrl: String?
    )
}