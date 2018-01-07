package org.demoth.aworlds.server2.model;

import org.demoth.aworlds.server2.api.messaging.Message;
import org.demoth.aworlds.server2.api.messaging.fromClient.CommandMessage;
import org.demoth.aworlds.server2.api.messaging.fromServer.AppearData;
import org.springframework.web.socket.WebSocketSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Player extends Actor {

    public boolean appeared = false;

    public ConcurrentLinkedQueue<CommandMessage> commands = new ConcurrentLinkedQueue<>();

    // this cache holds appear data sent from server to client.
    // scope - session
    public Map<String, AppearData> cache = new HashMap<>();

    // this set holds ids of object that were visible previous frame.
    // used to calculate appear/disappear data
    // scope - frame
    public Set<String> sightLastFrame = new TreeSet<>();

    private BlockingQueue<Message> results = new LinkedBlockingQueue<>();

    private Location location;

    private WebSocketSession session;

    public WebSocketSession getSession() {
        return session;
    }

    public void setSession(WebSocketSession session) {
        this.session = session;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public void enqueueRequest(CommandMessage command) {
        commands.add(command);
    }

    public void enqueueResponse(Message response) {
        results.add(response);
    }

    public boolean idle() {
        return commands.isEmpty();
    }

    public Message getUpdate() throws InterruptedException {
        return results.take();
    }
}
