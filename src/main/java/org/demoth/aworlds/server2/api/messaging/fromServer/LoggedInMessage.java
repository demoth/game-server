package org.demoth.aworlds.server2.api.messaging.fromServer;

import org.demoth.aworlds.server2.api.messaging.MapLike;

import java.util.Collection;
import java.util.Map;

public class LoggedInMessage extends MapLike {
    public static final String TYPE = "LOGGED_IN";
    public Collection<String> characters;

    public LoggedInMessage(Collection<String> characters) {
        this.characters = characters;
    }

    public LoggedInMessage(Map<String, Object> from) {
        super(from);
        this.characters = (Collection<String>) from.get("characters");
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> result = super.toMap();
        result.put("object_type", TYPE);
        result.put("characters", characters);
        return result;
    }
}
