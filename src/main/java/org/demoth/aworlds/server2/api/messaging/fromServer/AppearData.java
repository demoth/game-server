package org.demoth.aworlds.server2.api.messaging.fromServer;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.demoth.aworlds.server2.api.messaging.Message;

public class AppearData extends Message {
    @JsonProperty("object_type")
    public String objectType;
    public String id;
    public long x;
    public long y;

    public AppearData() {
    }

    public AppearData(String objectType, String id, long x, long y) {
        this.objectType = objectType;
        this.id = id;
        this.x = x;
        this.y = y;
    }
}
