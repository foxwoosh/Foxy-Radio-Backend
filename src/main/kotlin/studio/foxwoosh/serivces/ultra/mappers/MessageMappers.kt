package studio.foxwoosh.serivces.ultra.mappers

import studio.foxwoosh.serivces.ultra.client_responses.CurrentTrackResponse
import studio.foxwoosh.serivces.ultra.client_responses.PreviousTrackResponse
import studio.foxwoosh.serivces.ultra.messages.UltraMessageType
import studio.foxwoosh.serivces.ultra.messages.UltraSongDataMessage

fun CurrentTrackResponse.mapToMessage() = UltraSongDataMessage(
    UltraMessageType.SONG_DATA,
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

fun PreviousTrackResponse.mapToMessage() = UltraSongDataMessage.PreviousTrack(
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