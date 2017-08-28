package org.demoth.aworlds.server2.model;

import org.demoth.aworlds.server2.api.Message;

public class Player extends Actor {
    private Location location;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void enqueue(Message command) {
    }
}
