package org.demoth.aworlds.server2.api.messaging;

import org.demoth.aworlds.server2.api.messaging.fromClient.JoinMessage;
import org.demoth.aworlds.server2.api.messaging.fromClient.LoginMessage;
import org.demoth.aworlds.server2.api.messaging.fromServer.JoinedMessage;
import org.demoth.aworlds.server2.api.messaging.fromServer.LoggedInMessage;
import org.demoth.aworlds.server2.api.messaging.fromServer.UpdateMessage;

import java.util.Map;

public class MessageParser {
    public static MapLike fromMap(Map<String, Object> src) {
        String type = (String) src.get("type");
        switch (type) {
            case LoginMessage.TYPE:
                return new LoginMessage(src);
            case LoggedInMessage.TYPE:
                return new LoggedInMessage(src);
            case JoinMessage.TYPE:
                return new JoinMessage(src);
            case JoinedMessage.TYPE:
                return new JoinedMessage(src);
            case UpdateMessage.TYPE: {
                return new UpdateMessage(src);
            }
            default:
                return null;
        }
    }
}
