package org.demoth.aworlds.server2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.demoth.aworlds.server2.api.Message;
import org.demoth.aworlds.server2.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.Set;

import static org.demoth.aworlds.server2.api.MessageType.*;

public class MessageHandler extends TextWebSocketHandler {

    private static final Logger LOG = LoggerFactory.getLogger(MessageHandler.class);

    @Autowired
    UserService userService;

    final ObjectMapper mapper;

    public MessageHandler() {
        mapper = new ObjectMapper();
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        LOG.debug("Received: " + message.getPayload() + " from: " + session.getId());
        try {
            Message request = mapper.readValue(message.getPayload().toString(), Message.class);
            switch (request.type) {
                case LOGIN:
                    User user = userService.login(request.params[0], request.params[1]);
                    if (user != null) {
                        String[] characters = userService.register(user, session.getId());
                        session.sendMessage(new Message(LOGGED_IN, characters).toText(mapper));
                        LOG.debug("User joined: " + session.getId());
                    } else {
                        LOG.debug("Wrong user/pass");
                        session.sendMessage(new Message(ERROR, "Wrong user/pass").toText(mapper));
                    }
                    break;
                case TEXT:
                    LOG.debug(request.params[0]);
            }
        } catch (Exception e) {
            LOG.error("Error while processing message", e);
        }
    }
}
