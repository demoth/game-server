package org.demoth.gameserver.model

enum class Direction {
    UP, DOWN, LEFT, RIGHT,
    NONE
}

data class Cell(
        val x: Int,
        val y: Int,
        val actors: MutableCollection<Actor> = mutableListOf(),
        var direction: Direction = Direction.NONE) {

    lateinit var region: Region

    init {
        actors.forEach { it.cell = this }
    }

    override fun toString(): String {
        return "Cell($x:$y)"
    }
}