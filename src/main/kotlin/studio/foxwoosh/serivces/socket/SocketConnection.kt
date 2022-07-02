package studio.foxwoosh.serivces.socket

import io.ktor.websocket.*
import java.util.*
import kotlin.collections.HashMap

class SocketConnection(val session: DefaultWebSocketSession) {
    val id = UUID.randomUUID().toString()
    val clientInfo = ClientInfo()

    var userID = 0L

    operator fun set(key: String, value: String) {
        clientInfo[key] = value
    }

    operator fun get(key: String) = clientInfo[key]

    override fun toString(): String {
        return "connection_id: $id\n${clientInfo}\n" +
                if (userID > 0) {
                    "user is logged in: $userID"
                } else {
                    "user is not logged in"
                }
    }
    class ClientInfo : HashMap<String, String> {

        constructor() : super()
        constructor(map: Map<String, String>) : this() {
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