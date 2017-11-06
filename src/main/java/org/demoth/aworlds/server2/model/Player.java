package org.demoth.aworlds.server2.model;

import org.demoth.aworlds.server2.api.Message;
import org.springframework.web.socket.WebSocketSession;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Player extends Actor {

    private ConcurrentLinkedQueue<Message> commands = new ConcurrentLinkedQueue<>();

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

    public void enqueueRequest(Message command) {
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
