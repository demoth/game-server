package org.demoth.aworlds.server2.model;

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

    public void update() {
        update(new TreeSet<>());
    }

    private void update(Collection<String> visited) {
        for (Actor actor : actors) {
            if (!visited.add(actor.id)) {
                return;
            } else {
                actor.update(visited);
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
