package org.demoth.aworlds.server2.model;

import org.demoth.aworlds.server2.api.LongPropertiesEnum;
import org.demoth.aworlds.server2.api.Message;
import org.demoth.aworlds.server2.api.MessageType;

import java.util.*;

public class Actor {
    private String name;
    public Callback onUpdate;
    private EnumMap<LongPropertiesEnum, Long> longProps = new EnumMap<>(LongPropertiesEnum.class);

    // updates accumulated during current frame
    private Collection<Message> updates = new ArrayList<>();

    public Actor(String name, Callback update) {
        this();
        this.name = name;
        onUpdate = update;
    }

    public Actor() {
        // todo use safe generator
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

    void updateTree(Collection<String> visited) {
        if (onUpdate != null) {
            onUpdate.run();
        }
        for (Actor actor : actors) {
            if (visited.add(actor.id))
                actor.updateTree(visited);
        }
    }

    protected void collectResults(Collection<Message> results, Collection<String> visited) {
        results.addAll(updates);
        updates.clear();
        for (Actor actor : actors) {
            if (visited.add(actor.id))
                actor.collectResults(results, visited);
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

    private Collection<Message> getUpdates() {
        return updates;
    }

    public void setLong(LongPropertiesEnum key, Long value) {
        Long oldValue = longProps.put(key, value);
        updates.add(new Message(MessageType.UPDATE, id, key.name(), String.valueOf(oldValue), String.valueOf(value)));
    }

    public Long getLong(LongPropertiesEnum key) {
        return longProps.get(key);
    }

    public void addActor(Actor a) {
        getActors().add(a);
    }
}
