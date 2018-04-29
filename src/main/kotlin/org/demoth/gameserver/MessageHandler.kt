package org.demoth.gameserver

import com.fasterxml.jackson.databind.ObjectMapper
import org.demoth.gameserver.api.messaging.*
import org.demoth.gameserver.model.Player
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap

open class MessageHandler : TextWebSocketHandler() {
    @Autowired
    private var userService: UserService? = null
    @Autowired
    private var actorService: ActorService? = null
    @Autowired
    private var locationWorkerManager: LocationWorkerManager? = null
    @Autowired
    private var updateSenderManager: UpdateSenderManager? = null

    private val mapper: ObjectMapper = ObjectMapper()
    private var players = ConcurrentHashMap<String, Player>()

    @Throws(Exception::class)
    override fun afterConnectionClosed(session: WebSocketSession?, status: CloseStatus?) {
        if (session == null || status == null)
            return
        super.afterConnectionClosed(session, status)
        players[session.id]?.let { player ->
            player.location?.remove(player)
            players.remove(session.id)
            updateSenderManager?.stopSendingUpdates(player)
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
                    LOG.info("User logged in: {}", session.id)
                } else {
                    // LOG.debug("Wrong login/pass");
                    // session.sendMessage(new FromServerMessage(ERROR, "Wrong login/pass").toText(mapper));
                }

            } else if (request is JoinMessage) {
                val character = actorService!!.loadCharacter(request.character_id)
                character.session = session
                players[session.id] = character
                LOG.info("Character joined {}", character)
                if (character.location == null) {
                    actorService!!.setLocation(character)
                }
                character.location!!.add(character)
                LOG.info("Location loaded {}", character.location)
                session.sendMessage(TextMessage(mapper.writeValueAsString(JoinedMessage(character.id))))
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
