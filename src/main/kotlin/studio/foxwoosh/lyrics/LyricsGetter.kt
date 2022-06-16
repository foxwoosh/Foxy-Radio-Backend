package studio.foxwoosh.lyrics

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import org.jsoup.Jsoup
import org.jsoup.nodes.TextNode
import studio.foxwoosh.AppJson
import studio.foxwoosh.database.LyricsDao
import studio.foxwoosh.http_responses.LyricsResponse

fun Application.installLyricsGetter() {
    routing {
        get("/lyrics") {
            call.respondText {
                val artist = call.request.queryParameters["artist"]
                val title = call.request.queryParameters["title"]

                if (artist.isNullOrEmpty() || title.isNullOrEmpty())
                    return@respondText AppJson.encodeToString(LyricsResponse.empty)

                var lyrics = ""

                val cachedOriginal = LyricsDao.get(artist, title)
                if (cachedOriginal != null) {
                    // try to find lyrics in database with original parameters
                    lyrics = cachedOriginal.lyrics
                } else {
                    // if original wasn't found then try to find
                    // with fixed parameters (without some symbols etc)
                    val fixedTitle = fixQuery(title)
                    val fixedArtist = fixQuery(artist)

                    if (fixedTitle != title || fixedArtist != artist) {
                        // try to look up database only if something was fixed in title or artist
                        LyricsDao.get(fixedArtist, fixedTitle)?.let {
                            lyrics = it.lyrics
                        }
                    }

                    // if lyrics still empty it's time to look up online
                    if (lyrics.isEmpty()) {
                        lyrics = try {
                            // first try
                            findLyricsOnline(artist, title).also {
                                LyricsDao.save(artist, title, it)
                            }
                        } catch (e: Exception) {
                            try {
                                findLyricsOnline(fixedArtist, fixedTitle).also {
                                    LyricsDao.save(fixedArtist, fixedTitle, it)
                                }
                            } catch (e: Exception) {
                                ""
                            }
                        }
                    }
                }

                AppJson.encodeToString(LyricsResponse(lyrics))
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
    val url = "${System.getenv("LYRICS_URL")}/?query=$query&type=all"

    val searchPage = Jsoup.connect(url).get()

    val lyricsUrl = searchPage.select("a[style][class][href][title]")
        .first { it.attr("href").contains("songmeanings.com/songs/view") }
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

    return sb.toString()
}