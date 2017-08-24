package org.demoth.aworlds.server2.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.TextMessage;

public class Message {
    public MessageType type;
    public String[] params;

    public Message() {
    }

    public Message(MessageType type, String... params) {
        this.type = type;
        this.params = params;
    }

    public TextMessage toText(ObjectMapper m) throws JsonProcessingException {
        return new TextMessage(m.writeValueAsBytes(this));
    }
}
