package org.demoth.aworlds.server2.api.messaging;

import java.util.HashMap;
import java.util.Map;

public abstract class MapLike {
    public MapLike() {
    }

    public MapLike(Map<String, Object> from) {
    }

    public abstract String getType();

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("type", getType());
        return result;
    }

    @Override
    public String toString() {
        return toMap().toString();
    }
}
