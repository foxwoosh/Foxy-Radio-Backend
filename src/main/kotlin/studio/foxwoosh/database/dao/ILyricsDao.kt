package studio.foxwoosh.database.dao

import studio.foxwoosh.database.tables.LyricData

interface ILyricsDao {
    suspend fun get(artist: String, title: String): LyricData?
    suspend fun save(artist: String, title: String, lyrics: String)
}