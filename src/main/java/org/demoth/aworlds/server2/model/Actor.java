package org.demoth.aworlds.server2.model;

import org.demoth.aworlds.server2.api.LongPropertiesEnum;
import org.demoth.aworlds.server2.api.messaging.Message;
import org.demoth.aworlds.server2.api.messaging.fromServer.StateChangeData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.UUID;

public class Actor {
    public Callback onUpdate;
    private String name;
    private EnumMap<LongPropertiesEnum, Long> longProps = new EnumMap<>(LongPropertiesEnum.class);

    // updates accumulated during current frame
    private Collection<Message> updates = new ArrayList<>();
    private String type;
    private String id;
    private Collection<Actor> actors;

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

    public Collection<Actor> getActors() {
        return actors;
    }

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLong(LongPropertiesEnum key, Long value) {
        Long oldValue = longProps.put(key, value);
        updates.add(new StateChangeData(id, key.name(), String.valueOf(value)));
    }

    public Long getLong(LongPropertiesEnum key) {
        return longProps.get(key);
    }

    public void addActor(Actor a) {
        getActors().add(a);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
