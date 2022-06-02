package studio.foxwoosh.ultra.socket_outcomes

import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
data class UltraSongDataSocketOutcome(
    @SerialName("album") val album: String? = null,
    @SerialName("artist") val artist: String,
    @SerialName("cover") val cover: String,
    @SerialName("prev_tracks") val previousTracks: List<PreviousTrack>,
    @SerialName("root") val root: String,
    @SerialName("title") val title: String,
    @SerialName("itunes_url") val iTunesUrl: String?,
    @SerialName("spotify_url") val spotifyUrl: String?,
    @SerialName("yamusic_url") val yandexMusicUrl: String?,
    @SerialName("youtube_url") val youtubeUrl: String?,
    @SerialName("ytmusic_url") val youtubeMusicUrl: String?
) {
    @kotlinx.serialization.Serializable
    data class PreviousTrack(
        @SerialName("artist") val artist: String,
        @SerialName("cover") val cover: String,
        @SerialName("title") val title: String,
        @SerialName("itunes_url") val itunesUrl: String?,
        @SerialName("spotify_url") val spotifyUrl: String?,
        @SerialName("yamusic_url") val yandexMusicUrl: String?,
        @SerialName("youtube_url") val youtubeUrl: String?,
        @SerialName("ytmusic_url") val youtubeMusicUrl: String?
    )
}