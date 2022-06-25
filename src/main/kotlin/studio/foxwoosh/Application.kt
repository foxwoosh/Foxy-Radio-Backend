package studio.foxwoosh

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import studio.foxwoosh.admin.installAdminProvider
import studio.foxwoosh.database.AppDatabase
import studio.foxwoosh.lyrics.lyricsGetter
import studio.foxwoosh.auth.auth
import studio.foxwoosh.ultra.Connection
import studio.foxwoosh.ultra.ultraWebsocket
import java.util.*

fun main() {
    embeddedServer(
        Netty,
        host = System.getenv("APP_HOST"),
        port = System.getenv("APP_PORT").toInt(),
    ) {
        val connectionsHolder = Collections.synchronizedSet<Connection?>(LinkedHashSet())

        AppDatabase.init()

        install(ContentNegotiation) { json(AppJson) }

        auth()

        lyricsGetter()
        ultraWebsocket(connectionsHolder)

        installAdminProvider(connectionsHolder)
    }.start(wait = true)
}