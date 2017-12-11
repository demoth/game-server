package org.demoth.aworlds.server2.api.messaging.fromClient;

import org.demoth.aworlds.server2.api.messaging.Message;

public class JoinMessage extends Message {
    public String characterId;

    public JoinMessage() {
    }

    public JoinMessage(String characterId) {
        this.characterId = characterId;
    }
}
