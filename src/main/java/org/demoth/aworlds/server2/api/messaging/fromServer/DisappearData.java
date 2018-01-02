package org.demoth.aworlds.server2.api.messaging.fromServer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.demoth.aworlds.server2.api.messaging.Message;

public class DisappearData extends Message implements Positioned {
    public String id;
    @JsonIgnore
    public long x;
    @JsonIgnore
    public long y;

    @Override
    public long getX() {
        return x;
    }

    @Override
    public long getY() {
        return y;
    }

    public DisappearData(String id, long x, long y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }
}