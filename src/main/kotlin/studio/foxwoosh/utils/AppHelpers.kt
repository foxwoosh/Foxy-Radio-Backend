package studio.foxwoosh.utils

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

val AppJson = Json {
    ignoreUnknownKeys = true
    prettyPrint = true
    isLenient = true
}

val AppHttpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(AppJson)
    }
}