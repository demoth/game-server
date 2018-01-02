package org.demoth.aworlds.server2.api.messaging.fromServer;

import org.demoth.aworlds.server2.api.messaging.Message;

import java.util.List;

public class UpdateMessage extends Message {
    public List<Message> updates;

    public UpdateMessage() {
    }

    public UpdateMessage(List<Message> updates) {
        this.updates = updates;
    }
}
