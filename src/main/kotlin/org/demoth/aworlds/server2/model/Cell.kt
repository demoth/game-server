package org.demoth.aworlds.server2.model

import java.util.ArrayList
import java.util.Arrays

class Cell(vararg actors: Actor) {
    internal var actors: MutableCollection<Actor> = ArrayList()

    init {
        this.actors.addAll(Arrays.asList(*actors))
    }
}
