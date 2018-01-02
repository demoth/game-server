package org.demoth.aworlds.server2.api.messaging.fromClient;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.demoth.aworlds.server2.api.messaging.Message;

public class JoinMessage extends Message {
    @JsonProperty("character_id")
    public String characterId;

    public JoinMessage() {
    }

    public JoinMessage(String characterId) {
        this.characterId = characterId;
    }
}
