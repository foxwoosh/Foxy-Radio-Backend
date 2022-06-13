package studio.foxwoosh

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import studio.foxwoosh.plugins.configureRouting
import studio.foxwoosh.plugins.configureSockets
import studio.foxwoosh.ultra.ultraWebsocketRouting

fun main() {
    embeddedServer(
        Netty,
        host = "127.0.0.1",
        port = 8080,
    ) {
        configureRouting()
        configureSockets()

        ultraWebsocketRouting()
    }.start(wait = true)
}