package org.demoth.aworlds.server2.api.messaging.fromClient;

import org.demoth.aworlds.server2.api.messaging.Message;

public class CommandMessage extends Message {
    public Message action;

    @Override
    public String toString() {
        return "CommandMessage{" +
                "action=" + action +
                '}';
    }
}
