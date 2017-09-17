package org.demoth.aworlds.server2.model;

public class Location extends Actor {
    private char[][] terrain;
    private Location location;

    public Location() {
        // todo remove
        String[] location = new String[6];
        location[0] = "####################";
        location[1] = "#....#####.#########";
        location[2] = "##....#.##........##";
        location[3] = "##....##....###...##";
        location[4] = "#.##.........##...##";
        location[5] = "####################";
        terrain = new char[6][];
        for (int i = 0; i < 6; i++) {
            terrain[i] = location[i].toCharArray();
        }
    }

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
