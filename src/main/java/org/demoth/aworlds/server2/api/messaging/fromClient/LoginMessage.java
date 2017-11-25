package org.demoth.aworlds.server2.api.messaging.fromClient;

import org.demoth.aworlds.server2.api.messaging.MapLike;

import java.util.Map;

public class LoginMessage extends MapLike {
    public static final String TYPE = "LOGIN";
    public String user;
    public String password;

    public LoginMessage(String user, String password) {
        this.user = user;
        this.password = password;
    }

    public LoginMessage(Map<String, Object> from) {
        super(from);
        this.user = (String) from.get("login");
        this.password = (String) from.get("password");
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> result = super.toMap();
        result.put("login", user);
        result.put("password", password);
        return result;
    }
}
