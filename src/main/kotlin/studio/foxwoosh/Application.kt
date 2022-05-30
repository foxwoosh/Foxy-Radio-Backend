package studio.foxwoosh

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.serialization.json.Json
import studio.foxwoosh.plugins.configureRouting
import studio.foxwoosh.plugins.configureSockets
import studio.foxwoosh.ultra.ultraWebsocketRouting

fun main() {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                }
            )
        }
    }

    embeddedServer(
        Netty,
        port = System.getenv("PORT").toInt(),
    ) {
        configureRouting()
        configureSockets()

        ultraWebsocketRouting(client)
    }.start(wait = true)
}