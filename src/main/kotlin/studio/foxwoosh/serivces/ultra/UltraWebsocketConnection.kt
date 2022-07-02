package studio.foxwoosh.serivces.ultra

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import studio.foxwoosh.utils.AppHttpClient
import studio.foxwoosh.utils.AppJson
import studio.foxwoosh.utils.sendText
import studio.foxwoosh.serivces.ultra.client_responses.CurrentTrackResponse
import studio.foxwoosh.serivces.ultra.client_responses.UniqueIdResponse
import studio.foxwoosh.serivces.ultra.mappers.mapToMessage
import studio.foxwoosh.serivces.ultra.messages.UltraSongDataMessage
import studio.foxwoosh.serivces.ultra.socket_incomes.ParametrizedMessage
import java.time.Duration

private val ultraPollingScope = object : CoroutineScope {
    override val coroutineContext = SupervisorJob() + Dispatchers.IO
}

fun Application.webSocket(connections: MutableSet<Connection>) {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
        contentConverter = KotlinxWebsocketSerializationConverter(AppJson)
    }

    var pollingJob: Job? = null
    var lastFetchedData: UltraSongDataMessage? = null

    routing {
        webSocket("/ultra") {
            val connection = Connection(this)

            lastFetchedData?.let {
                println("WebSocket: send last fetched to new client")
                outgoing.sendText(AppJson.encodeToString(it))
            }

            if (addConnection(connections, connection) && pollingJob?.isActive != true) {
                pollingJob = pollingJob(
                    getId = {
                        AppHttpClient
                            .get("https://meta.fmgid.com/stations/ultra/id.json?t=${System.currentTimeMillis()}")
                            .body<UniqueIdResponse>()
                            .uniqueID
                    },
                    fetch = {
                        val response = AppHttpClient
                            .get("https://meta.fmgid.com/stations/ultra/current.json?t=${System.currentTimeMillis()}")
                            .body<CurrentTrackResponse>()

                        lastFetchedData = response.mapToMessage().also { data ->
                            val dataString = AppJson.encodeToString(data)
                            println("WebSocket: sending track data - ${data.title} - ${data.artist}")

                            connections.forEach {
                                it.session.outgoing.sendText(dataString)
                            }
                        }
                    }
                )
            }

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
                        ParametrizedMessage.Type.UNSUBSCRIBE -> {
                            println("WebSocket: client unsubscribed from connection ${connection.id}")
                            close(CloseReason(CloseReason.Codes.NORMAL, "Unsubscribe"))
                        }
                    }
                }
            } catch (e: Exception) {
                println("WebSocket: socket error, ${e.message}")
            } finally {
                if (removeConnection(connections, connection)) {
                    println("WebSocket: polling stopped")
                    pollingJob?.cancel()
                    pollingJob = null
                    lastFetchedData = null
                }
            }
        }
    }
}

/**
 * @return - should start polling
 */
private fun addConnection(connections: MutableSet<Connection>, connection: Connection): Boolean {
    connections.add(connection)
    println("WebSocket: added connection, count = ${connections.size}")
    return connections.size == 1
}

/**
 * @return - should stop polling
 */

private fun removeConnection(connections: MutableSet<Connection>, connection: Connection): Boolean {
    connections.remove(connection)
    println("WebSocket: removed connection, count = ${connections.size}")
    return connections.isEmpty()
}

private fun pollingJob(
    getId: suspend () -> String,
    fetch: suspend () -> Unit
) = ultraPollingScope.launch {
    println("WebSocket: polling started")

    var currentUniqueID: String? = null

    while (isActive) {
        try {
            val fetchedUniqueID = getId()

            if (fetchedUniqueID != currentUniqueID) {
                println("WebSocket: fetching track")
                fetch()
                currentUniqueID = fetchedUniqueID
            }

            delay(10000)
        } catch (e: Exception) {
            delay(5000)
            
            println("WebSocket: fetching failed, ${e.message}")
        }
    }
}

