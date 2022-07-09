package studio.foxwoosh.serivces.lyrics

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import org.jsoup.Jsoup
import org.jsoup.nodes.TextNode
import studio.foxwoosh.utils.AppJson
import studio.foxwoosh.database.LyricsDao
import studio.foxwoosh.database.LyricsReportsDao
import studio.foxwoosh.database.tables.LyricData
import studio.foxwoosh.http_responses.LyricsResponse
import studio.foxwoosh.serivces.auth.ValidatedUserPrincipal

fun Application.lyricsGetter() {
    routing {
        authenticate(optional = true) {
            get("/v1/lyrics") {
                val artist = call.request.queryParameters["artist"]
                val title = call.request.queryParameters["title"]

                if (artist.isNullOrEmpty() || title.isNullOrEmpty()) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }

                var lyrics = LyricsDao.get(artist, title)

                if (lyrics == null) {
                    // if original wasn't found then try to find
                    // with fixed parameters (without some symbols etc)
                    val fixedTitle = fixQuery(title)
                    val fixedArtist = fixQuery(artist)

                    if (fixedTitle != title || fixedArtist != artist) {
                        // try to look up database only if something was fixed in title or artist
                        LyricsDao.get(fixedArtist, fixedTitle)?.let {
                            lyrics = it
                        }
                    }

                    // if still not found, look up online
                    if (lyrics == null) {
                        lyrics = try {
                            // first try with original data
                            val foundOriginal = findLyricsOnline(artist, title)
                            println("LYRICS: saved-O")
                            LyricsDao.save(artist, title, foundOriginal)
                        } catch (e: Exception) {
                            try {
                                // another try with fixed
                                val foundFixed = findLyricsOnline(fixedArtist, fixedTitle)
                                println("LYRICS: saved-F")
                                LyricsDao.save(fixedArtist, fixedTitle, foundFixed)
                            } catch (e: Exception) {
                                // :(
                                println("LYRICS: no lyrics found for $title by $artist")
                                null
                            }
                        }
                    }
                }

                // nothing was found, saving empty lyrics in database to make it available for report
                if (lyrics == null) {
                    lyrics = LyricsDao.save(artist, title, "")
                }

                lyrics?.let { lyricData ->
                    val user = call.principal<ValidatedUserPrincipal>()

                    val report = user?.let { LyricsReportsDao.get(user.id, lyricData.id) }

                    call.respond(
                        HttpStatusCode.OK,
                        LyricsResponse(
                            lyricData.id,
                            lyricData.lyrics,
                            report?.state,
                            report?.moderatorComment
                        )
                    )
                } ?: call.respond(HttpStatusCode.BadRequest)
            }
        }
    }
}

private val fixQueryRegex = Regex("\\(.*?\\)")

private fun fixQuery(q: String) =
    q.replace(fixQueryRegex, "")
        .replace("&", " ")

private fun findLyricsOnline(artist: String, title: String): String {
    val query = "$artist $title".replace(" ", "+")
    val url = "https://${System.getenv("LYRICS_URL")}/query/?query=$query&type=all"

    val searchPage = Jsoup.connect(url).get()

    val lyricsUrl = searchPage.select("a[style][class][href][title]")
        .first { it.attr("href").contains("${System.getenv("LYRICS_URL")}/songs/view") }
        .attr("href")

    val lyricsPage = Jsoup.connect("https:$lyricsUrl").get()

    val lyricsElements = lyricsPage.select("div[class]")
        .first { it.attr("class") == "holder lyric-box" }
        .childNodes()
        .filterIsInstance<TextNode>()

    val sb = StringBuilder()

    lyricsElements.forEach {
        sb.append(it.wholeText.trim())
            .append("\n")
    }

    return sb.toString().trim()
}