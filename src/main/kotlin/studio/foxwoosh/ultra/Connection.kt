package studio.foxwoosh.ultra

import io.ktor.websocket.*
import java.util.UUID

class Connection(val session: DefaultWebSocketSession) {
    val id = UUID.randomUUID().toString()
    var clientInfo: String? = null
}