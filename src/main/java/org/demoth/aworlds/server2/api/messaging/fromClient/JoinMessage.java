package org.demoth.aworlds.server2.api.messaging.fromClient;

import org.demoth.aworlds.server2.api.messaging.MapLike;

import java.util.Map;

public class JoinMessage extends MapLike {
    public static final String TYPE = "JOIN";
    public String characterId;

    public JoinMessage(String characterId) {
        this.characterId = characterId;
    }

    public JoinMessage(Map<String, Object> from) {
        super(from);
        this.characterId = (String) from.get("character_id");
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> result = super.toMap();
        result.put("character_id", characterId);
        return result;
    }
}
