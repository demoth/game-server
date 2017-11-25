package org.demoth.aworlds.server2.api.messaging.fromServer;

import org.demoth.aworlds.server2.api.messaging.MapLike;

import java.util.Map;

public class JoinedMessage extends MapLike {
    public static final String TYPE = "JOINED";
    public Long x;
    public Long y;

    public JoinedMessage(Long x, Long y) {
        super();
        this.x = x;
        this.y = y;
    }

    public JoinedMessage(Map<String, Object> from) {
        super(from);
        this.x = Long.parseLong(from.get("X").toString());
        this.y = Long.parseLong(from.get("Y").toString());
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> result = super.toMap();
        result.put("X", String.valueOf(x));
        result.put("Y", String.valueOf(y));
        return result;
    }
}
