package studio.foxwoosh.serivces.socket

import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.decodeFromString
import studio.foxwoosh.ClientsConnections
import studio.foxwoosh.serivces.socket.messages.incoming.ParametrizedMessage
import studio.foxwoosh.serivces.socket.stations.UltraFetcher
import studio.foxwoosh.utils.AppJson
import java.time.Duration

private val ultraFetcher = UltraFetcher()
fun Application.webSocket() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
        contentConverter = KotlinxWebsocketSerializationConverter(AppJson)
    }

    routing {
        webSocket("/ultra") {
            val connection = SocketConnection(this)

            ClientsConnections.add(connection)

            try {
                for (frame in incoming) {
                    val text = (frame as? Frame.Text?)?.readText() ?: continue
                    val message = AppJson.decodeFromString<ParametrizedMessage>(text)

                    when (message.type) {
                        ParametrizedMessage.Type.SUBSCRIBE -> {
                            connection.clientInfo.putAll(message.params)
                        }
                        ParametrizedMessage.Type.LOGGED_USER_DATA -> {
                            connection.userID = message.params["id"]?.toLong() ?: 0
                        }
                        ParametrizedMessage.Type.USER_LOGOUT -> {
                            connection.userID = 0
                        }
                        ParametrizedMessage.Type.STATION_SELECT -> {
                            val station = Station.get(message.params["station"]?.toInt() ?: -1)

                            println("WebSocket: message STATION_SELECT - $station")

                            when (station) {
                                Station.ULTRA -> ultraFetcher.subscribe(connection)
                                null -> ultraFetcher.unsubscribe(connection)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                println("WebSocket: socket error, ${e.message}")
            } finally {
                ultraFetcher.unsubscribe(connection)
                ClientsConnections.remove(connection)
            }
        }
    }
}

