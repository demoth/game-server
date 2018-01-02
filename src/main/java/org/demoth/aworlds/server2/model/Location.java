package org.demoth.aworlds.server2.model;

import org.demoth.aworlds.server2.api.messaging.Message;
import org.demoth.aworlds.server2.api.messaging.fromClient.CommandMessage;
import org.demoth.aworlds.server2.api.messaging.fromClient.MoveAction;
import org.demoth.aworlds.server2.api.messaging.fromServer.AppearData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.demoth.aworlds.server2.api.LongPropertiesEnum.X;
import static org.demoth.aworlds.server2.api.LongPropertiesEnum.Y;

public class Location extends Actor {
    // players are kept to manage connection
    private Collection<Player> players = new ConcurrentLinkedQueue<>();
    private final Cell[][] board;

    public Location() {
        // todo remove
        int size = 6;
        setName("Test location");
        char[][] location = new char[size][];
        board = new Cell[size][];
        location[0] = "######".toCharArray();
        location[1] = "#...##".toCharArray();
        location[2] = ".....#".toCharArray();
        location[3] = ".....#".toCharArray();
        location[4] = "#.#...".toCharArray();
        location[5] = "######".toCharArray();
        for (int y = 0; y < size; y++) {
            board[y] = new Cell[size];
            char[] chars = location[y];
            for (int x = 0; x < size; x++) {
                char c = chars[x];
                Actor floorTile = new Actor();
                floorTile.setLong(X, (long) x);
                floorTile.setLong(Y, (long) y);
                floorTile.setType(c == '#' ? "WALL" : "FLOOR");
                getActors().add(floorTile);
                board[y][x] = new Cell(floorTile);
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
        // get the results of above updates
        collectResults(result, new TreeSet<>());
        Stream<AppearData> updates = getActors().stream().map(actor -> new AppearData(actor.getType(), actor.getId(), actor.getLong(X), actor.getLong(Y)));
        // todo: filter out redundant events
        result.addAll(updates.collect(toList()));
        return result;
    }

    private void performCommands() {
        for (Player player : players) {
            boolean performed = false;
            while (true) {
                CommandMessage command = player.commands.peek();
                if (command == null)
                    break;

                // todo implement real check
                if (performed)
                    break;
                else {
                    command = player.commands.poll();
                    performed = true;
                }

                System.out.println("executing: " + command);
                if (command.action instanceof MoveAction) {
                    MoveAction move = (MoveAction) command.action;
                    switch (move.direction) {
                        case "n":
                            move(player, -1, 0);
                            break;
                        case "s":
                            move(player, 1, 0);
                            break;
                        case "e":
                            move(player, 0, 1);
                            break;
                        case "w":
                            move(player, 0, -1);
                            break;
                    }
                }
            }
        }
    }

    private void move(Player player, int y, int x) {
        player.setLong(X, player.getLong(X) + x);
        player.setLong(Y, player.getLong(Y) + y);
        // todo: update board!
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