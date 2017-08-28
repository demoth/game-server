package org.demoth.aworlds.server2.model;

import java.util.Collection;

public class Actor {
    private String id;

    private Collection<Actor> actors;

    public void update(Collection<String> visited) {
        for (Actor actor : actors) {
            if (!visited.add(actor.id)) {
                return;
            } else {
                actor.update(visited);
            }
        }
    }

}
