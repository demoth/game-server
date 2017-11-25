package org.demoth.aworlds.server2.api.messaging.fromServer;

import org.demoth.aworlds.server2.api.messaging.MapLike;

import java.util.Map;

public class AppearData extends MapLike {
    public static final String TYPE = "APPEAR";
    String object_type;
    String id;
    long x;
    long y;

    public AppearData(String object_type, String id, long x, long y) {
        this.object_type = object_type;
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public AppearData(Map<String, Object> from) {
        super(from);
        this.object_type = (String) from.get("object_type");
        this.id = (String) from.get("object_id");
        this.x = Long.parseLong(from.get("x").toString());
        this.y = Long.parseLong(from.get("y").toString());
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> result = super.toMap();
        result.put("object_type", object_type);
        result.put("object_id", id);
        result.put("x", x);
        result.put("y", y);
        return result;
    }
}
