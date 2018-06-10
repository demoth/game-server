package org.demoth.gameserver.api.messaging

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.web.socket.BinaryMessage
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketMessage
import java.nio.charset.Charset

val mapper = ObjectMapper()

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
        JsonSubTypes.Type(value = LoginMessage::class, name = "login"),
        JsonSubTypes.Type(value = LoggedInMessage::class, name = "logged_in"),
        JsonSubTypes.Type(value = JoinMessage::class, name = "join"),
        JsonSubTypes.Type(value = JoinedMessage::class, name = "joined"),
        JsonSubTypes.Type(value = UpdateMessage::class, name = "update"),
        JsonSubTypes.Type(value = AppearData::class, name = "appear"),
        JsonSubTypes.Type(value = StateChangeData::class, name = "change"),
        JsonSubTypes.Type(value = Movement::class, name = "movement"),
        JsonSubTypes.Type(value = DisappearData::class, name = "disappear"),
        JsonSubTypes.Type(value = ErrorMessage::class, name = "error"),
        JsonSubTypes.Type(value = MoveAction::class, name = "move"))
abstract class Message {
    fun encode(): WebSocketMessage<*> {
        return TextMessage(mapper.writeValueAsString(this))
    }
}

data class ErrorMessage(val text: String = "") : Message()

fun decode(data: WebSocketMessage<*>): Message {
    val body = when (data) {
        is TextMessage -> data.payload.toString()
        is BinaryMessage -> String(data.payload.array(), Charset.forName("UTF-8"))
        else -> throw IllegalStateException("Illegal data type: ${data::class}")
    }
    return mapper.readValue(body, Message::class.java)
}
