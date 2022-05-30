package studio.foxwoosh

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.serialization.json.Json
import studio.foxwoosh.client_responses.UniqueIdResponse
import studio.foxwoosh.plugins.configureRouting
import studio.foxwoosh.plugins.configureSockets

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
        host = "0.0.0.0"
    ) {
        configureRouting()
        configureSockets(
            getId = {
                client
                    .get("https://meta.fmgid.com/stations/ultra/id.json")
                    .body<UniqueIdResponse>()
                    .uniqueID
            },
            getData = {
                client
                    .get("https://meta.fmgid.com/stations/ultra/current.json")
                    .body()
            }
        )
    }.start(wait = true)
}