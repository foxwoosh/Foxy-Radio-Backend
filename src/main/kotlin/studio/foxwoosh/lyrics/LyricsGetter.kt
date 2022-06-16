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

                val cachedOriginal = LyricsDao.get(artist, title)
                val lyrics = if (cachedOriginal != null) {
                    cachedOriginal.lyrics
                } else {
                    val fixedTitle = fixQuery(title)
                    val fixedArtist = fixQuery(artist)

                    val cachedFixed = LyricsDao.get(fixedArtist, fixedTitle)
                    cachedFixed?.lyrics
                        ?: try {
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
    val url = "https://songmeanings.com/query/?query=$query&type=all"

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