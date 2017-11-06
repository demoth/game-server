package org.demoth.aworlds.server2.model;

import org.demoth.aworlds.server2.api.LongPropertiesEnum;
import org.demoth.aworlds.server2.api.Message;
import org.demoth.aworlds.server2.api.MessageType;

import java.security.acl.LastOwnerException;
import java.util.*;
import java.util.concurrent.Callable;

public class Actor {
    private String name;
    public Callback onUpdate;
    private EnumMap<LongPropertiesEnum, Long> longProps = new EnumMap<>(LongPropertiesEnum.class);

    // updates accumulated during current frame
    private Collection<Message> updates = new ArrayList<>();

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

    protected void updateTree(Collection<String> visited, Collection<Message> result) {
        if (onUpdate != null) {
            onUpdate.run();
            result.addAll(getUpdates());
        }
        for (Actor actor : actors) {
            if (!visited.add(actor.id)) {
                return;
            } else {
//                if (actor.onUpdate != null) {
//                    actor.onUpdate.run();
//                    result.addAll(actor.getUpdates());
//                }
                actor.updateTree(visited, result);
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

    public Collection<Message> getUpdates() {
        return updates;
    }

    public void setLong(LongPropertiesEnum key, Long value) {
        Long oldValue = longProps.put(key, value);
        updates.add(new Message(MessageType.UPDATE, id, key.name(), String.valueOf(oldValue), String.valueOf(value)));
    }

    public Long getLong(LongPropertiesEnum key) {
        return longProps.get(key);
    }

    public void clearUpdates() {
        updates.clear();
    }
}
