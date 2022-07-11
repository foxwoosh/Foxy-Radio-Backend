package studio.foxwoosh.serivces.socket

import io.ktor.websocket.*
import java.util.*

class SocketConnection(val session: DefaultWebSocketSession) {
    val id = UUID.randomUUID().toString()
    val clientInfo = ClientInfo()

    var userID = 0L
    var subscribedStation: Station? = null

    operator fun set(key: String, value: String) {
        clientInfo[key] = value
    }

    operator fun get(key: String) = clientInfo[key]

    override fun toString(): String {
        val sb = StringBuilder( "connection_id: $id\n${clientInfo}\n")
        sb.append(
            if (userID > 0) {
                "user is logged in: $userID"
            } else {
                "user is not logged in"
            }
        )

        subscribedStation?.let {
            sb.append("\n")
            sb.append("listening to: $it")
        }

        return sb.toString()
    }
    class ClientInfo : HashMap<String, String?> {

        constructor() : super()
        constructor(map: Map<String, String?>) : this() {
            putAll(map)
        }
        override fun toString(): String {
            val sb = StringBuilder()

            entries.forEach { (key, value) ->
                sb.append("$key: $value \n")
            }

            return sb.toString().trim()
        }
    }
}