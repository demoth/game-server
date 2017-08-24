package org.demoth.aworlds.server2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.demoth.aworlds.server2.api.Message;
import org.demoth.aworlds.server2.api.MessageType;
import org.demoth.aworlds.server2.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class MessageHandler extends TextWebSocketHandler {

    @Autowired
    UserService userService;

    final ObjectMapper mapper;

    public MessageHandler() {
        mapper = new ObjectMapper();
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        System.out.println("Received: " + message.getPayload());
        try {
            Message request = mapper.readValue(message.getPayload().toString(), Message.class);
            switch (request.type) {
                case LOGIN:
                    User user = userService.login(request.params[0], request.params[1]);
                    if (user != null) {
                        userService.register(user, session.getId());
                    } else {
                        session.sendMessage(new Message(MessageType.ERROR, "Wrong user/pass").toText(mapper));
                    }
                    break;
                case TEXT:
                    System.out.println(request.params[0]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
