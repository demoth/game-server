package org.demoth.gameserver.model

data class Region(
        val cells: MutableCollection<Cell> = mutableSetOf(),
        val id: String = IdGenerator.newUUID()
) {
    override fun toString(): String {
        return "Region($id)"
    }

    override fun equals(other: Any?): Boolean {
        return if (other is Region)
            id == other.id
        else false
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}