package org.demoth.aworlds.server2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.demoth.aworlds.server2.api.Request;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class ActionHandler extends TextWebSocketHandler {
    final ObjectMapper mapper;

    public ActionHandler() {
        mapper = new ObjectMapper();
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        System.out.println("Received: " + message.getPayload());
        try {
            Request request = mapper.readValue(message.getPayload().toString(), Request.class);
            switch (request.type) {

                case LOGIN:
                    String user = request.params[0];
                    String pass = request.params[1];
                    System.out.println("Logged user: " + user);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
