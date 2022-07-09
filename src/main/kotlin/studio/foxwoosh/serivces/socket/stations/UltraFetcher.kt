package studio.foxwoosh.serivces.socket.stations

import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString
import studio.foxwoosh.ClientsConnections
import studio.foxwoosh.serivces.socket.SocketConnection
import studio.foxwoosh.serivces.socket.Station
import studio.foxwoosh.serivces.socket.client_responses.CurrentTrackResponse
import studio.foxwoosh.serivces.socket.client_responses.UniqueIdResponse
import studio.foxwoosh.serivces.socket.mappers.mapToMessage
import studio.foxwoosh.serivces.socket.messages.outgoing.SongDataMessage
import studio.foxwoosh.utils.AppHttpClient
import studio.foxwoosh.utils.AppJson
import studio.foxwoosh.utils.sendText
import java.util.concurrent.atomic.AtomicInteger

class UltraFetcher {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()

    private val subscribersCount = AtomicInteger(0)

    private var pollingJob: Job? = null
    private var lastFetchedData: SongDataMessage? = null

    /**
     * @return - true if polling started
     */
    suspend fun subscribe(connection: SocketConnection) = mutex.withLock {
        if (connection.subscribedStation == Station.ULTRA) return@withLock false

        connection.subscribedStation = Station.ULTRA

        val currentSubscribers = subscribersCount.incrementAndGet()

        println("WebSocket: subscribe ($currentSubscribers)")

        if (currentSubscribers == 1) {
            start()

            true
        } else {
            lastFetchedData?.let {
                println("WebSocket: send last fetched to new client")
                connection.session.outgoing.sendText(AppJson.encodeToString(it))
            }

            false
        }
    }

    /**
     * @return - true if polling stopped
     */
    suspend fun unsubscribe(connection: SocketConnection) = mutex.withLock {
        if (connection.subscribedStation == null) return@withLock false

        val currentSubscribers = subscribersCount.decrementAndGet()

        connection.subscribedStation = null

        println("WebSocket: unsubscribe ($currentSubscribers)")

        if (currentSubscribers == 0) {
            pollingJob?.cancel()
            pollingJob = null
            lastFetchedData = null

            println("WebSocket: polling stopped")

            true
        } else false
    }

    private fun start() {
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

                    ClientsConnections
                        .filter { it.subscribedStation == Station.ULTRA }
                        .forEach {
                            it.session.outgoing.sendText(dataString)
                        }
                }
            }
        )
    }

    private fun pollingJob(
        getId: suspend () -> String,
        fetch: suspend () -> Unit
    ) = coroutineScope.launch {
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
}