package studio.foxwoosh.ultra

import io.ktor.websocket.*
import java.util.*
import kotlin.collections.HashMap

class Connection(val session: DefaultWebSocketSession) {
    val id = UUID.randomUUID().toString()
    val clientInfo = ClientInfo()

    operator fun set(key: String, value: String) {
        clientInfo[key] = value
    }

    operator fun get(key: String) = clientInfo[key]

    override fun toString(): String {
        return "Client ID: $id\n${clientInfo}"
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