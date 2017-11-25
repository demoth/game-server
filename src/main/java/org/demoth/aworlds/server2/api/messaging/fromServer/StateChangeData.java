package org.demoth.aworlds.server2.api.messaging.fromServer;

import org.demoth.aworlds.server2.api.messaging.MapLike;

import java.util.Map;

public class StateChangeData extends MapLike {
    public static final String TYPE = "CHANGE";
    String id;
    String field;
    String newValue;

    public StateChangeData(String id, String field, String newValue) {
        this.id = id;
        this.field = field;
        this.newValue = newValue;
    }

    public StateChangeData(Map<String, Object> from) {
        super(from);
        this.id = (String) from.get("id");
        this.field = (String) from.get("field");
        this.newValue = (String) from.get("new_value");
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> result = super.toMap();
        result.put("object_id", id);
        result.put("field", field);
        result.put("new_value", newValue);
        return result;
    }
}
