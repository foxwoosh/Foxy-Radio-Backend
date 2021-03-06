package studio.foxwoosh

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import studio.foxwoosh.database.AppDatabase
import studio.foxwoosh.serivces.auth.auth
import studio.foxwoosh.serivces.lyrics.lyricsGetter
import studio.foxwoosh.serivces.lyrics.lyricsReports
import studio.foxwoosh.serivces.monitor.monitor
import studio.foxwoosh.serivces.socket.SocketConnection
import studio.foxwoosh.serivces.socket.webSocket
import studio.foxwoosh.serivces.utils.utils
import studio.foxwoosh.utils.AppJson
import java.util.*

val ClientsConnections: MutableSet<SocketConnection> = Collections.synchronizedSet(LinkedHashSet())

fun main() {
    embeddedServer(
        Netty,
        host = System.getenv("APP_HOST"),
        port = System.getenv("APP_PORT").toInt(),
    ) {
        AppDatabase.init()

        install(ContentNegotiation) { json(AppJson) }

        auth()
        lyricsReports()
        lyricsGetter()
        utils()

        webSocket()

        monitor()
    }.start(wait = true)
}