package studio.foxwoosh.plugins

import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration
import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import studio.foxwoosh.client_responses.CurrentTrackResponse

fun Application.configureSockets(
    getId: suspend () -> String,
    getData: suspend () -> CurrentTrackResponse
) {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    var fetchJob: Job? = null

    routing {
        webSocket("/") { // websocketSession
            for (frame in incoming) {
                val text = (frame as? Frame.Text?)?.readText() ?: continue

                when (text) {
                    "connect" -> {
                        outgoing.send(Frame.Text("You are connected"))

                        if (fetchJob?.isActive != true) {
                            fetchJob = fetchJob(getId) {
                                val data = getData()
                                val string = Json.encodeToString(data)
                                println("sending: \n $string")

                                outgoing.send(Frame.Text(string))
                            }
                        }
                    }
                    "bye" -> {
                        outgoing.send(Frame.Text("You are disconnected"))

                        fetchJob?.cancel()
                        fetchJob = null

                        close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                    }
                }
            }
        }
    }
}

private fun fetchJob(
    getId: suspend () -> String,
    fetch: suspend () -> Unit
) = GlobalScope.launch {
    println("starting fetch")

    var currentUniqueID: String? = null

    while (isActive) {
        try {
            val fetchedUniqueID = getId()

            println("fetched id. current == $currentUniqueID, new == $fetchedUniqueID")

            if (fetchedUniqueID != currentUniqueID) {
                fetch()
                currentUniqueID = fetchedUniqueID
            }
        } catch (e: Exception) {
            println(
                "error, ${e.message}"
            )
        }

        delay(10000)
    }
}

