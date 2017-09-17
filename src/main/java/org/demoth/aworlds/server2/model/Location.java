package org.demoth.aworlds.server2.model;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Location extends Actor {
    private char[][] terrain;
    private Location location;
    private Collection<Player> players = new ConcurrentLinkedQueue<>();

    public Location() {
        // todo remove
        int size = 6;
        setName("Test location");
        String[] location = new String[size];
        location[0] = "####################";
        location[1] = "#....#####.#########";
        location[2] = "##....#.##........##";
        location[3] = "##....##....###...##";
        location[4] = "#.##.........##...##";
        location[5] = "####################";
        terrain = new char[size][];
        for (int i = 0; i < size; i++) {
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

    public Collection<Player> getPlayers() {
        return players;
    }

    public long getSleepTime(boolean allPlayersReady) {
        // todo: remove
        if (allPlayersReady)
            return 100;
        else
            return 2000;
    }

    public void add(Actor actor) {
        if (actor instanceof Player) {
            players.add((Player) actor);
        }
        getActors().add(actor);
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }
}