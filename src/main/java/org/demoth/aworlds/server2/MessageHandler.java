package org.demoth.aworlds.server2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.demoth.aworlds.server2.api.Message;
import org.demoth.aworlds.server2.model.Location;
import org.demoth.aworlds.server2.model.Player;
import org.demoth.aworlds.server2.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

import static org.demoth.aworlds.server2.api.MessageType.*;

public class MessageHandler extends TextWebSocketHandler {

    private static final Logger LOG = LoggerFactory.getLogger(MessageHandler.class);

    @Autowired
    UserService userService;

    @Autowired
    ActorService actorService;

    @Autowired
    WorkManager workManager;

    final ObjectMapper mapper;

    ConcurrentHashMap<String, Player> players = new ConcurrentHashMap<>();

    public MessageHandler() {
        mapper = new ObjectMapper();
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);
        Player player = players.get(session.getId());
        player.getLocation().removePlayer(player);
        players.remove(player);
        LOG.info("Disconnected: {}", session.getId());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        LOG.debug("Received: {} from {}", message.getPayload(), session.getId());
        try {
            Message request = mapper.readValue(message.getPayload().toString(), Message.class);
            switch (request.type) {
                case LOGIN:
                    User user = userService.login(request.params[0], request.params[1]);
                    if (user != null) {
                        String[] characters = userService.register(user, session.getId());
                        session.sendMessage(new Message(LOGGED_IN, characters).toText(mapper));
                        LOG.debug("User logged in: {}", session.getId());
                    } else {
                        LOG.debug("Wrong user/pass");
                        session.sendMessage(new Message(ERROR, "Wrong user/pass").toText(mapper));
                    }
                    break;

                case JOIN:
                    Player character;
                    if (request.params.length == 1) {
                        character = actorService.loadCharacter(request.params[0]);
                    } else {
                        character = actorService.createCharacter(request.params);
                    }
                    players.put(session.getId(), character);
                    LOG.debug("Character joined {}", character);
                    if (character.getLocation() == null) {
                        actorService.setLocation(character);
                    }
                    character.getLocation().add(character);
                    LOG.debug("Location loaded {}", character.getLocation());
                    session.sendMessage(new Message(JOINED, encodeLocation(character.getLocation())).toText(mapper));
                    workManager.runLocation(character.getLocation());
                    break;

                case COMMAND:
                    Player p = players.get(session.getId());
                    if (p != null)
                        p.enqueueRequest(request);
                    break;

                case TEXT:
                    LOG.debug(request.params[0]);
            }
        } catch (Exception e) {
            LOG.error("Error while processing message", e);
        }
    }

    private static String[] encodeLocation(Location location) {
        // todo sent only visible tiles
        String[] result = new String[location.getTerrain().length];
        char[][] terrain = location.getTerrain();
        for (int i = 0; i < terrain.length; i++) {
            char[] chars = terrain[i];
            result[i] = new String(chars);
        }
        return result;
    }
}
