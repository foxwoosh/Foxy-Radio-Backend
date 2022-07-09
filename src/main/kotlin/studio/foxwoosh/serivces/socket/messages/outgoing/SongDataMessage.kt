package studio.foxwoosh.serivces.socket.messages.outgoing

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import studio.foxwoosh.serivces.socket.messages.MessageType

@Serializable
data class SongDataMessage(
    @SerialName("type") val type: MessageType,
    @SerialName("id") val id: String,
    @SerialName("title") val title: String,
    @SerialName("artist") val artist: String,
    @SerialName("album") val album: String,
    @SerialName("cover") val cover: String,
    @SerialName("date") val date: String,
    @SerialName("time") val time: String,
    @SerialName("root") val root: String,
    @SerialName("metadata") val metadata: String,
    @SerialName("itunes_url") val iTunesUrl: String?,
    @SerialName("spotify_url") val spotifyUrl: String?,
    @SerialName("yamusic_url") val yandexMusicUrl: String?,
    @SerialName("youtube_url") val youtubeUrl: String?,
    @SerialName("ytmusic_url") val youtubeMusicUrl: String?
)