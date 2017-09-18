package org.demoth.aworlds.server2.model;

import org.demoth.aworlds.server2.api.Message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;
import java.util.UUID;

public class Actor {
    private String name;

    public Actor() {
        id = UUID.randomUUID().toString();
        actors = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Collection<Actor> getActors() {
        return actors;
    }

    public void setActors(Collection<Actor> actors) {
        this.actors = actors;
    }

    private String id;

    private Collection<Actor> actors;

    public Collection<Message> update() {
        Collection<Message> result = new ArrayList<>();
        update(new TreeSet<>(), result);
        return result;
    }

    protected void update(Collection<String> visited, Collection<Message> result) {
        for (Actor actor : actors) {
            if (!visited.add(actor.id)) {
                return;
            } else {
                actor.update(visited, result);
            }
        }
    }

    @Override
    public String toString() {
        return "Actor{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                '}';
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
