package org.demoth.gameserver.model

import java.util.*

class Cell(vararg actors: Actor) {
    internal var actors: MutableCollection<Actor> = ArrayList()

    init {
        this.actors.addAll(Arrays.asList(*actors))
    }
}
