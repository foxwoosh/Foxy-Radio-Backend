package studio.foxwoosh

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import studio.foxwoosh.database.AppDatabase
import studio.foxwoosh.lyrics.installLyricsGetter
import studio.foxwoosh.plugins.installSocket
import studio.foxwoosh.ultra.ultraWebsocket

fun main() {
    embeddedServer(
        Netty,
        host = System.getenv("APP_HOST"),
        port = System.getenv("APP_PORT").toInt(),
    ) {
        AppDatabase.init()

        installLyricsGetter()
        installSocket()

        ultraWebsocket()
    }.start(wait = true)
}