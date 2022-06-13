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
    var foundLyrics = 0
    var notFoundLyrics = 0

    routing {
        get("/lyrics") {
            call.respondText {
                val artist = call.request.queryParameters["artist"] ?: ""
                val title = call.request.queryParameters["title"] ?: ""

                val lyrics = try {
                    val lyrics = songMeanings(artist, title)
                    foundLyrics++
                    lyrics
                } catch (e: Exception) {
                    notFoundLyrics++
                    ""
                }

                println("LYRICS: this session found: $foundLyrics, not found: $notFoundLyrics")

                AppJson.encodeToString(LyricsResponse(lyrics))
            }
        }
    }
}

private fun songMeanings(artist: String, title: String): String {
    val query = "$artist $title".replace(" ", "+")
    val url = "https://songmeanings.com/query/?query=$query&type=all"

    val searchPage = Jsoup.connect(url).get()

    val lyricsUrl = searchPage.select("a[style][href][title]")
        .first {
            val elementTitle = it.attr("title")
            elementTitle.contains(title, ignoreCase = true) || title.contains(elementTitle, ignoreCase = true)
        }
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