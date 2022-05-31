package studio.foxwoosh.ultra

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import studio.foxwoosh.sendText
import studio.foxwoosh.ultra.http_responses.CurrentTrackResponse
import studio.foxwoosh.ultra.http_responses.UniqueIdResponse
import studio.foxwoosh.ultra.socket_incomes.SocketIncome
import studio.foxwoosh.ultra.socket_outcomes.UltraSocketOutcome
import studio.foxwoosh.ultra.socket_outcomes.UltraSocketOutcomeType
import java.util.*
import kotlin.collections.LinkedHashSet

private val ultraPollingScope = object : CoroutineScope {
    override val coroutineContext = SupervisorJob() + Dispatchers.IO
}

fun Application.ultraWebsocketRouting(httpClient: HttpClient) {
    var pollingJob: Job? = null
    var lastFetchedData: UltraSocketOutcome<CurrentTrackResponse>? = null

    routing {
        val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())

        webSocket("/ultra") {
            val connection = Connection(this)
            lastFetchedData?.let {
                println("Sending last fetched Ultra data to client ${connection.id}")
                outgoing.sendText(Json.encodeToString(it))
            }

            if (addConnection(connections, connection) && pollingJob?.isActive != true) {
                println("Starting polling")
                pollingJob = pollingJob(
                    getId = {
                        httpClient
                            .get("https://meta.fmgid.com/stations/ultra/id.json?t=${System.currentTimeMillis()}")
                            .body<UniqueIdResponse>()
                            .uniqueID
                    },
                    fetch = {
                        val data = httpClient
                            .get("https://meta.fmgid.com/stations/ultra/current.json?t=${System.currentTimeMillis()}")
                            .body<CurrentTrackResponse>()
                        lastFetchedData = UltraSocketOutcome(UltraSocketOutcomeType.SONG_DATA, data)

                        val string = Json.encodeToString(data)
                        println("sending: \n $string")

                        connections.forEach { it.session.outgoing.sendText(string) }
                    }
                )
            }

            try {
                for (frame in incoming) {
                    val text = (frame as? Frame.Text?)?.readText() ?: continue
                    val income = Json.decodeFromString<SocketIncome>(text)

                    when (income.type) {
                        SocketIncome.Type.SUBSCRIBE -> {
                            connection.clientInfo = income["info"]
                            println("Client subscribed to connection ${connection.id} with info ${connection.clientInfo}")
                        }
                        SocketIncome.Type.UNSUBSCRIBE -> {
                            println("Client unsubscribed from connection ${connection.id}")
                            close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                        }
                    }
                }
            } catch (e: Exception) {
                println("Error: ${e.message}")
            } finally {
                if (removeConnection(connections, connection)) {
                    println("Stop polling")
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
    println("Added connection. Count = ${connections.size}")
    return connections.size == 1
}

/**
 * @return - should stop polling
 */

private fun removeConnection(connections: MutableSet<Connection>, connection: Connection): Boolean {
    connections.remove(connection)
    println("Removed connection. Count = ${connections.size}")
    return connections.isEmpty()
}

private fun pollingJob(
    getId: suspend () -> String,
    fetch: suspend () -> Unit
) = ultraPollingScope.launch {
    println("Ultra polling started")

    var currentUniqueID: String? = null

    while (isActive) {
        try {
            val fetchedUniqueID = getId()

            if (fetchedUniqueID != currentUniqueID) {
                println("Fetching Ultra track")
                fetch()
                currentUniqueID = fetchedUniqueID
            }
        } catch (e: Exception) {
            println("error, ${e.message}")
        }

        delay(10000)
    }
}

