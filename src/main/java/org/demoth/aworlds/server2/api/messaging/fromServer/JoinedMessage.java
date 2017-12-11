package org.demoth.aworlds.server2.api.messaging.fromServer;

import org.demoth.aworlds.server2.api.messaging.Message;

public class JoinedMessage extends Message {
    public Long x;
    public Long y;

    public JoinedMessage() {
    }

    public JoinedMessage(Long x, Long y) {
        this.x = x;
        this.y = y;
    }
}
