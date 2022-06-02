package studio.foxwoosh.ultra.mappers

import studio.foxwoosh.ultra.http_responses.CurrentTrackResponse
import studio.foxwoosh.ultra.http_responses.PreviousTrackResponse
import studio.foxwoosh.ultra.socket_outcomes.UltraSocketOutcome
import studio.foxwoosh.ultra.socket_outcomes.UltraSocketOutcomeType
import studio.foxwoosh.ultra.socket_outcomes.UltraSongDataSocketOutcome

fun CurrentTrackResponse.map() = UltraSocketOutcome(
    UltraSocketOutcomeType.SONG_DATA,
    UltraSongDataSocketOutcome(
        album = album,
        artist = artist,
        cover = coverWebp,
        previousTracks = previousTracks.map { it.map() },
        root = root,
        title = title,
        iTunesUrl = iTunesUrl,
        spotifyUrl = spotifyUrl,
        yandexMusicUrl = yandexMusicUrl,
        youtubeUrl = youtubeUrl,
        youtubeMusicUrl = youtubeMusicUrl
    )
)

fun PreviousTrackResponse.map() = UltraSongDataSocketOutcome.PreviousTrack(
    artist = artist,
    cover = cover,
    title = title,
    itunesUrl = itunesUrl,
    spotifyUrl = spotifyUrl,
    yandexMusicUrl = yandexMusicUrl,
    youtubeUrl = youtubeUrl,
    youtubeMusicUrl = youtubeMusicUrl
)