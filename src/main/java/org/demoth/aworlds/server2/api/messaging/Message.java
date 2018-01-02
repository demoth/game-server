package org.demoth.aworlds.server2.api.messaging;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.demoth.aworlds.server2.api.messaging.fromClient.CommandMessage;
import org.demoth.aworlds.server2.api.messaging.fromClient.JoinMessage;
import org.demoth.aworlds.server2.api.messaging.fromClient.LoginMessage;
import org.demoth.aworlds.server2.api.messaging.fromClient.MoveAction;
import org.demoth.aworlds.server2.api.messaging.fromServer.*;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = LoginMessage.class, name = "login"),
        @JsonSubTypes.Type(value = LoggedInMessage.class, name = "logged_in"),
        @JsonSubTypes.Type(value = JoinMessage.class, name = "join"),
        @JsonSubTypes.Type(value = JoinedMessage.class, name = "joined"),

        @JsonSubTypes.Type(value = UpdateMessage.class, name = "update"),
        @JsonSubTypes.Type(value = AppearData.class, name = "appear"),
        @JsonSubTypes.Type(value = StateChangeData.class, name = "change"),
        @JsonSubTypes.Type(value = DisappearData.class, name = "disappear"),

        @JsonSubTypes.Type(value = CommandMessage.class, name = "command"),
        @JsonSubTypes.Type(value = MoveAction.class, name = "move")
})
public abstract class Message {
}
