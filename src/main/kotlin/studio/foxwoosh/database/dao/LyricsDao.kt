package studio.foxwoosh.database.dao

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import studio.foxwoosh.database.AppDatabase
import studio.foxwoosh.database.LyricsDispatcher
import studio.foxwoosh.database.tables.LyricData
import studio.foxwoosh.database.tables.Lyrics

interface ILyricsDao {
    suspend fun get(id: Int): LyricData?
    suspend fun get(artist: String, title: String): LyricData?
    suspend fun save(artist: String, title: String, lyrics: String): LyricData
}

class LyricsDao : ILyricsDao {
    override suspend fun get(id: Int): LyricData? = AppDatabase.query(LyricsDispatcher) {
        Lyrics
            .select { Lyrics.id eq id }
            .map { get(it) }
            .singleOrNull()
    }

    override suspend fun get(artist: String, title: String): LyricData? = AppDatabase.query(LyricsDispatcher) {
        Lyrics
            .select { Lyrics.id eq id(artist, title) }
            .map { get(it) }
            .singleOrNull()
    }

    override suspend fun save(artist: String, title: String, lyrics: String): LyricData {
        val id = id(artist, title)

        AppDatabase.query(LyricsDispatcher) {
            Lyrics.insert {
                it[Lyrics.id] = id
                it[Lyrics.artist] = artist
                it[Lyrics.title] = title
                it[Lyrics.lyrics] = lyrics
            }
        }

        return LyricData(id, title, artist, lyrics)
    }

    private fun get(row: ResultRow): LyricData {
        val artist = row[Lyrics.artist]
        val title = row[Lyrics.title]
        val id = id(artist, title)

        return LyricData(
            id = id,
            title = title,
            artist = artist,
            lyrics = row[Lyrics.lyrics]
        )
    }

    private fun id(artist: String, title: String) = "${artist.lowercase()}+${title.lowercase()}".hashCode()
}