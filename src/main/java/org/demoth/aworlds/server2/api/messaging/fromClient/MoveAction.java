package org.demoth.aworlds.server2.api.messaging.fromClient;

import org.demoth.aworlds.server2.api.messaging.Message;

public class MoveAction extends Message {
    public String direction;

    @Override
    public String toString() {
        return "MoveAction{" +
                "direction='" + direction + '\'' +
                '}';
    }
}
