package org.demoth.aworlds.server2.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class Cell {
    Collection<Actor> actors = new ArrayList<>();

    public Cell(Actor... actors) {
        this.actors.addAll(Arrays.asList(actors));
    }
}
