package org.demoth.aworlds.server2.api.messaging.fromClient;

import org.demoth.aworlds.server2.api.messaging.Message;

public class LoginMessage extends Message {
    public String user;
    public String password;

    public LoginMessage() {
    }

    public LoginMessage(String user, String password) {
        this.user = user;
        this.password = password;
    }
}
