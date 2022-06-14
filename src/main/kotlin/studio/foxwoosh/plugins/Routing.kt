package studio.foxwoosh.plugins

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.encodeToString
import org.jsoup.Jsoup
import org.jsoup.nodes.TextNode
import studio.foxwoosh.AppJson
import studio.foxwoosh.http_responses.LyricsResponse

fun Application.configureRouting() {
    val cachedLyrics = HashMap<String, String>()

    routing {
        get("/lyrics") {
            call.respondText {
                val artist = call.request.queryParameters["artist"] ?: ""
                val title = call.request.queryParameters["title"] ?: ""

                val cacheKey = "$artist - $title"

                val lyrics = try {
                    val cached = cachedLyrics[cacheKey]

                    if (cached.isNullOrEmpty()) {
                        songMeanings(artist, title).also {
                            cachedLyrics[cacheKey] = it
                        }
                    } else cached
                } catch (e: Exception) {
                    ""
                }

                AppJson.encodeToString(LyricsResponse(lyrics))
            }
        }
    }
}

private fun songMeanings(artist: String, title: String): String {
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