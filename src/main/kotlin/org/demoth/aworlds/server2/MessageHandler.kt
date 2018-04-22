package org.demoth.aworlds.server2

import com.fasterxml.jackson.databind.ObjectMapper
import org.demoth.aworlds.server2.api.LongPropertiesEnum.X
import org.demoth.aworlds.server2.api.LongPropertiesEnum.Y
import org.demoth.aworlds.server2.api.messaging.Message
import org.demoth.aworlds.server2.api.messaging.fromClient.CommandMessage
import org.demoth.aworlds.server2.api.messaging.fromClient.JoinMessage
import org.demoth.aworlds.server2.api.messaging.fromClient.LoginMessage
import org.demoth.aworlds.server2.api.messaging.fromServer.JoinedMessage
import org.demoth.aworlds.server2.api.messaging.fromServer.LoggedInMessage
import org.demoth.aworlds.server2.model.Player
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap

open class MessageHandler : TextWebSocketHandler() {
    internal val mapper: ObjectMapper
    @Autowired
    internal var userService: UserService? = null
    @Autowired
    internal var actorService: ActorService? = null
    @Autowired
    internal var locationWorkerManager: LocationWorkerManager? = null
    @Autowired
    internal var updateSenderManager: UpdateSenderManager? = null
    internal var players = ConcurrentHashMap<String, Player>()

    init {
        mapper = ObjectMapper()
    }

    @Throws(Exception::class)
    override fun afterConnectionClosed(session: WebSocketSession?, status: CloseStatus?) {
        super.afterConnectionClosed(session, status)
        players[session!!.id]?.let { player ->
            player.location!!.removePlayer(player)
            players.remove(session.id)
            updateSenderManager!!.stopSendingUpdates(player)
            LOG.info("Disconnected: {}", session.id)
        }
    }

    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        LOG.debug("Received: {} from {}", message.payload, session.id)
        try {
            val request = mapper.readValue(message.payload.toString(), Message::class.java)
            if (request is LoginMessage) {
                val user = userService!!.login(request.login, request.password)
                if (user != null) {
                    val characters = userService!!.register(user, session.id)
                    session.sendMessage(TextMessage(mapper.writeValueAsString(LoggedInMessage(characters))))
                    LOG.debug("User logged in: {}", session.id)
                } else {
                    // LOG.debug("Wrong login/pass");
                    // session.sendMessage(new FromServerMessage(ERROR, "Wrong login/pass").toText(mapper));
                }

            } else if (request is JoinMessage) {
                val character = actorService!!.loadCharacter(request.characterId)
                character.session = session
                players[session.id] = character
                LOG.debug("Character joined {}", character)
                if (character.location == null) {
                    actorService!!.setLocation(character)
                }
                character.location!!.add(character)
                LOG.debug("Location loaded {}", character.location)
                session.sendMessage(TextMessage(mapper.writeValueAsString(JoinedMessage(character.getLong(X), character.getLong(Y)))))
                locationWorkerManager!!.runLocation(character.location!!)
                updateSenderManager!!.startSendingUpdates(character)
            } else if (request is CommandMessage) {
                players[session.id]?.enqueueRequest(request)
            }
        } catch (e: Exception) {
            LOG.error("Error while processing message", e)
        }

    }

    companion object {

        private val LOG = LoggerFactory.getLogger(MessageHandler::class.java)
    }
}
