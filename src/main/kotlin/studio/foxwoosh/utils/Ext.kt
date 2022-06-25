package studio.foxwoosh.utils

import io.ktor.websocket.*
import kotlinx.coroutines.channels.SendChannel

suspend fun SendChannel<Frame>.sendText(text: String) {
    send(Frame.Text(text))
}