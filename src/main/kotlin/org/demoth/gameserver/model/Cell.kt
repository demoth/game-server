package org.demoth.gameserver.model

enum class Direction {
    UP, DOWN, LEFT, RIGHT,
    NONE
}

data class Cell(
        val x: Int,
        val y: Int,
        var region: Region,
        val id: String = IdGenerator.newUUID(),
        val actors: MutableCollection<Actor> = linkedSetOf(),
        var direction: Direction = Direction.NONE) {

    init {
        region.cells.add(this)
        actors.forEach { it.cell = this }
    }

    override fun toString(): String {
        return "Cell($x:$y)"
    }

    override fun equals(other: Any?): Boolean {
        return if (other is Cell)
            id == other
        else false
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
