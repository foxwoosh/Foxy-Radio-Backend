package studio.foxwoosh.ultra.mappers

import studio.foxwoosh.ultra.http_responses.CurrentTrackResponse
import studio.foxwoosh.ultra.http_responses.PreviousTrackResponse
import studio.foxwoosh.ultra.messages.UltraMessageType
import studio.foxwoosh.ultra.messages.UltraSongDataMessage

fun CurrentTrackResponse.map() = UltraSongDataMessage(
    UltraMessageType.SONG_DATA,
    id = uniqueID,
    album = album,
    artist = artist,
    cover = coverWebp,
    root = root,
    title = title,
    iTunesUrl = iTunesUrl,
    spotifyUrl = spotifyUrl,
    yandexMusicUrl = yandexMusicUrl,
    youtubeUrl = youtubeUrl,
    youtubeMusicUrl = youtubeMusicUrl,
    previousTracks = previousTracks.map { it.map() }
)

fun PreviousTrackResponse.map() = UltraSongDataMessage.PreviousTrack(
    artist = artist,
    cover = coverWebp,
    title = title,
    itunesUrl = itunesUrl,
    spotifyUrl = spotifyUrl,
    yandexMusicUrl = yandexMusicUrl,
    youtubeUrl = youtubeUrl,
    youtubeMusicUrl = youtubeMusicUrl
)