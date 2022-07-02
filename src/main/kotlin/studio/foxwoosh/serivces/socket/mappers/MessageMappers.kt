package studio.foxwoosh.serivces.socket.mappers

import studio.foxwoosh.serivces.socket.client_responses.CurrentTrackResponse
import studio.foxwoosh.serivces.socket.client_responses.PreviousTrackResponse
import studio.foxwoosh.serivces.socket.messages.MessageType
import studio.foxwoosh.serivces.socket.messages.outgoing.SongDataMessage

fun CurrentTrackResponse.mapToMessage() = SongDataMessage(
    type = MessageType.SONG_DATA,
    id = uniqueID,
    album = album,
    artist = artist ?: "No data",
    cover = coverWebp,
    root = root,
    title = title ?: "No data",
    iTunesUrl = iTunesUrl,
    spotifyUrl = spotifyUrl,
    yandexMusicUrl = yandexMusicUrl,
    youtubeUrl = youtubeUrl,
    youtubeMusicUrl = youtubeMusicUrl,
    date = date,
    time = time,
    metadata = metadata,
    previousTracks = previousTracks.map { it.mapToMessage() }
)

fun PreviousTrackResponse.mapToMessage() = SongDataMessage.PreviousTrack(
    artist = artist,
    cover = coverWebp,
    title = title,
    date = date,
    time = time,
    itunesUrl = itunesUrl,
    spotifyUrl = spotifyUrl,
    yandexMusicUrl = yandexMusicUrl,
    youtubeUrl = youtubeUrl,
    youtubeMusicUrl = youtubeMusicUrl
)