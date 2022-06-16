package studio.foxwoosh.ultra

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import studio.foxwoosh.AppHttpClient
import studio.foxwoosh.AppJson
import studio.foxwoosh.sendText
import studio.foxwoosh.ultra.client_responses.CurrentTrackResponse
import studio.foxwoosh.ultra.client_responses.UniqueIdResponse
import studio.foxwoosh.ultra.mappers.mapToMessage
import studio.foxwoosh.ultra.messages.UltraSongDataMessage
import studio.foxwoosh.ultra.socket_incomes.ParametrizedMessage
import java.util.*

private val ultraPollingScope = object : CoroutineScope {
    override val coroutineContext = SupervisorJob() + Dispatchers.IO
}

fun Application.ultraWebsocket() {
    var pollingJob: Job? = null
    var lastFetchedData: UltraSongDataMessage? = null

    routing {
        val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())

        webSocket("/ultra") {
            val connection = Connection(this)

            lastFetchedData?.let {
                println("ULTRA: send last fetched to client: ${connection.id}")

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
                            println("ULTRA: sending track data - ${data.title} by ${data.artist}")

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
                            connection.clientInfo = message["info"]
                            println("ULTRA: client subscribed to connection ${connection.id} with info ${connection.clientInfo}")
                        }
                        ParametrizedMessage.Type.UNSUBSCRIBE -> {
                            println("ULTRA: client unsubscribed from connection ${connection.id}")
                            close(CloseReason(CloseReason.Codes.NORMAL, "Unsubscribe"))
                        }
                    }
                }
            } catch (e: Exception) {
                println("ULTRA: socket error, ${e.message}")
            } finally {
                if (removeConnection(connections, connection)) {
                    println("ULTRA: polling stopped")
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
    println("ULTRA: added connection, count = ${connections.size}")
    return connections.size == 1
}

/**
 * @return - should stop polling
 */

private fun removeConnection(connections: MutableSet<Connection>, connection: Connection): Boolean {
    connections.remove(connection)
    println("ULTRA: removed connection, count = ${connections.size}")
    return connections.isEmpty()
}

private fun pollingJob(
    getId: suspend () -> String,
    fetch: suspend () -> Unit
) = ultraPollingScope.launch {
    println("ULTRA: polling started")

    var currentUniqueID: String? = null

    while (isActive) {
        try {
            val fetchedUniqueID = getId()

            if (fetchedUniqueID != currentUniqueID) {
                println("ULTRA: fetching track")
                fetch()
                currentUniqueID = fetchedUniqueID
            }

            delay(10000)
        } catch (e: Exception) {
            println("ULTRA: fetching failed, ${e.message}")

            delay(5000)
        }
    }
}

