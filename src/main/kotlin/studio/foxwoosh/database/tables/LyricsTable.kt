package studio.foxwoosh.database.tables

import org.jetbrains.exposed.sql.Table

data class LyricData(val id: Int, val title: String, val artist: String, val lyrics: String)

object Lyrics : Table() {
    val id = integer("id")
    val title = text("title")
    val artist = text("artist")
    val lyrics = text("lyrics")

    override val primaryKey = PrimaryKey(id)
}