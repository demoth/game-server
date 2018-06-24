package org.demoth.gameserver

import com.fasterxml.jackson.databind.ObjectMapper
import org.demoth.gameserver.api.messaging.*
import org.demoth.gameserver.model.Player
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.socket.*
import org.springframework.web.socket.handler.AbstractWebSocketHandler
import java.util.concurrent.ConcurrentHashMap

open class MessageHandler : AbstractWebSocketHandler() {
    @Autowired
    lateinit var userService: UserService
    @Autowired
    lateinit var actorService: ActorService
    @Autowired
    lateinit var locationWorkerManager: LocationWorkerManager
    @Autowired
    lateinit var updateSenderManager: UpdateSenderManager

    private val mapper: ObjectMapper = ObjectMapper()
    private var players = ConcurrentHashMap<String, Player>()

    override fun afterConnectionClosed(session: WebSocketSession?, status: CloseStatus?) {
        if (session == null)
            return
        players[session.id]?.let { player ->
            player.location?.remove(player)
            players.remove(session.id)
            updateSenderManager.stopSendingUpdates(player)
            LOG.info("Disconnected: {}", session.id)
        }
    }

    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        LOG.debug("Received: {} from {}", message::class.simpleName, session.id)
        try {
            val body = when (message) {
                is TextMessage -> message.payload.toString()
                is BinaryMessage -> String(message.payload.array())
                else -> {
                    LOG.debug("Unsupported message type: $message")
                    return
                }
            }
            val request = mapper.readValue(body, Message::class.java)
            if (request is LoginMessage) {
                val user = userService.login(request.login, request.password)
                if (user != null) {
                    val characters = userService.register(user, session.id)
                    session.sendMessage(TextMessage(mapper.writeValueAsString(LoggedInMessage(characters))))
                    LOG.info("User logged in: {}", session.id)
                } else {
                    LOG.debug("Wrong credentials")
                    session.sendMessage(TextMessage(mapper.writeValueAsString(ErrorMessage("Wrong credentials"))))
                    session.close()
                }

            } else if (request is JoinMessage) {
                val character = actorService.loadCharacter(request.character_id)
                character.session = session
                players[session.id] = character
                if (character.location == null) {
                    actorService.setLocation(character)
                }
                LOG.info("Character joined {}", character)
                session.sendMessage(TextMessage(mapper.writeValueAsString(JoinedMessage(character.id))))
                locationWorkerManager.runLocation(character.location!!)
                updateSenderManager.startSendingUpdates(character)
            } else if (request is MoveAction) {
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
