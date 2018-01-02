package org.demoth.aworlds.server2.api.messaging.fromClient;

import org.demoth.aworlds.server2.api.messaging.Message;

public class LoginMessage extends Message {
    public String login;
    public String password;

    public LoginMessage() {
    }

    public LoginMessage(String login, String password) {
        this.login = login;
        this.password = password;
    }
}
