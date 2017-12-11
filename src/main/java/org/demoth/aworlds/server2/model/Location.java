package org.demoth.aworlds.server2.model;

import org.demoth.aworlds.server2.api.messaging.Message;
import org.demoth.aworlds.server2.api.messaging.fromServer.AppearData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.util.stream.Collectors.toList;
import static org.demoth.aworlds.server2.api.LongPropertiesEnum.X;
import static org.demoth.aworlds.server2.api.LongPropertiesEnum.Y;

public class Location extends Actor {
    // players are kept to manage connection
    private Collection<Player> players = new ConcurrentLinkedQueue<>();

    public Location() {
        // todo remove
        int size = 6;
        setName("Test location");
        char[][] location = new char[size][];
        location[0] = "####################".toCharArray();
        location[1] = "#....#####.#########".toCharArray();
        location[2] = "##....#.##........##".toCharArray();
        location[3] = "##....##....###...##".toCharArray();
        location[4] = "#.##.........##...##".toCharArray();
        location[5] = "####################".toCharArray();
        for (int y = 0; y < size; y++) {
            char[] chars = location[y];
            for (int x = 0; x < chars.length; x++) {
                char c = chars[x];
                Actor tile = new Actor();
                tile.setLong(X, (long) x);
                tile.setLong(Y, (long) y);
                tile.setType(c == '#' ? "WALL" : "FLOOR");
                getActors().add(tile);
                // todo appear!
            }
        }
    }

    public Collection<Player> getPlayers() {
        return players;
    }

    public Collection<Message> updateLocation() {
        Collection<Message> result = new ArrayList<>();
        // invoke onUpdate() callback on the whole tree
        updateTree(new TreeSet<>());
        // process requests for actors
        performCommands();
        // get the results of above changes
        collectResults(result, new TreeSet<>());
        result.addAll(toAppearMessage(getActors()));
        return result;
    }

    private Collection<Message> toAppearMessage(Collection<Actor> actors) {
        return actors.stream().map(actor -> new AppearData(actor.getType(), actor.getId(), actor.getLong(X), actor.getLong(Y))).collect(toList());
    }

    private void performCommands() {
        //for (Actor actor : getActors()) {
        // for each command:
        //  - discard if requirements are not matched
        //  - perform
        //}
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