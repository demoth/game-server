package org.demoth.aworlds.server2.model;

public class Location extends Actor {
    private char[][] terrain;
    private Location location;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public char[][] getTerrain() {
        return terrain;
    }
}
