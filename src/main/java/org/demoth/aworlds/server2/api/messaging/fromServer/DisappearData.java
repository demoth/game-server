package org.demoth.aworlds.server2.api.messaging.fromServer;

import org.demoth.aworlds.server2.api.messaging.MapLike;

import java.util.Map;

public class DisappearData extends MapLike {
    public static final String TYPE = "DISAPPEAR";

    String id;

    public DisappearData(String id) {
        this.id = id;
    }

    public DisappearData(Map<String, Object> from) {
        super(from);
        this.id = (String) from.get("object_id");
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> result = super.toMap();
        result.put("object_id", id);
        return result;
    }
}
