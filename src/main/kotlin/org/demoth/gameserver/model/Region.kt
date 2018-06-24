package org.demoth.gameserver.model

data class Region(
        val cells: MutableCollection<Cell> = mutableListOf(),
        val id: String = IdGenerator.newUUID()
)