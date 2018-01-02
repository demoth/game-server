package org.demoth.aworlds.server2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.demoth.aworlds.server2.api.messaging.Message;
import org.demoth.aworlds.server2.api.messaging.fromClient.JoinMessage;
import org.demoth.aworlds.server2.api.messaging.fromClient.LoginMessage;
import org.demoth.aworlds.server2.api.messaging.fromServer.JoinedMessage;
import org.demoth.aworlds.server2.api.messaging.fromServer.LoggedInMessage;
import org.demoth.aworlds.server2.model.Player;
import org.demoth.aworlds.server2.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.demoth.aworlds.server2.api.LongPropertiesEnum.X;
import static org.demoth.aworlds.server2.api.LongPropertiesEnum.Y;

public class MessageHandler extends TextWebSocketHandler {

    private static final Logger LOG = LoggerFactory.getLogger(MessageHandler.class);
    final ObjectMapper mapper;
    @Autowired
    UserService userService;
    @Autowired
    ActorService actorService;
    @Autowired
    LocationWorkerManager locationWorkerManager;
    @Autowired
    UpdateSenderManager updateSenderManager;
    ConcurrentHashMap<String, Player> players = new ConcurrentHashMap<>();

    public MessageHandler() {
        mapper = new ObjectMapper();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        Player player = players.get(session.getId());
        player.getLocation().removePlayer(player);
        players.remove(session.getId());
        updateSenderManager.stopSendingUpdates(player);
        LOG.info("Disconnected: {}", session.getId());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        LOG.debug("Received: {} from {}", message.getPayload(), session.getId());
        try {
            Message request = mapper.readValue(message.getPayload().toString(), Message.class);
            if (request instanceof LoginMessage) {
                LoginMessage login = (LoginMessage) request;
                User user = userService.login(login.login, login.password);
                if (user != null) {
                    List<String> characters = userService.register(user, session.getId());
                    session.sendMessage(new TextMessage(mapper.writeValueAsString(new LoggedInMessage(characters))));
                    LOG.debug("User logged in: {}", session.getId());
                } else {
//                        LOG.debug("Wrong login/pass");
//                        session.sendMessage(new FromServerMessage(ERROR, "Wrong login/pass").toText(mapper));
                }

            } else if (request instanceof JoinMessage) {
                JoinMessage join = (JoinMessage) request;
                Player character = actorService.loadCharacter(join.characterId);
                character.setSession(session);
                players.put(session.getId(), character);
                LOG.debug("Character joined {}", character);
                if (character.getLocation() == null) {
                    actorService.setLocation(character);
                }
                character.getLocation().add(character);
                LOG.debug("Location loaded {}", character.getLocation());
                session.sendMessage(new TextMessage(mapper.writeValueAsString(new JoinedMessage(character.getLong(X), character.getLong(Y)))));
                locationWorkerManager.runLocation(character.getLocation());
                updateSenderManager.startSendingUpdates(character);
            }
        } catch (Exception e) {
            LOG.error("Error while processing message", e);
        }
    }
}
