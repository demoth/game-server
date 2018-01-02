package org.demoth.aworlds.server2.api.messaging.fromServer;

import org.demoth.aworlds.server2.api.messaging.Message;

import java.util.Collection;
public class LoggedInMessage extends Message {
    public Collection<String> characters;

    public LoggedInMessage() {
    }

    public LoggedInMessage(Collection<String> characters) {
        this.characters = characters;
    }

}
