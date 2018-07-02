package org.demoth.gameserver.model

import org.demoth.gameserver.api.ActorType
import org.demoth.gameserver.api.PropertyLong
import org.demoth.gameserver.api.messaging.Movement
import org.demoth.gameserver.api.messaging.StateChangeData
import org.demoth.gameserver.api.messaging.Update
import java.util.*

open class Actor(
        val type: ActorType,
        val properties: EnumMap<PropertyLong, Long> = EnumMap<PropertyLong, Long>(PropertyLong::class.java),
        var onUpdate: (() -> Unit)? = null)
    : Entity() {

    // updates accumulated during current frame
    private val updates = ArrayList<Update>()

    // common properties
    var cell: Cell? = null

    val actors: MutableList<Actor> = ArrayList()

    override fun update() {
        onUpdate?.invoke()
        actors.forEach { it.update() }
    }

    /**
     * Move actor to specific position. Network updates will be generated.
     * Used to move objects during game.
     */
    fun move(cell: Cell) {
        this.cell?.actors?.remove(this)
        this.cell = cell
        cell.actors.add(this)
        updates.add(Movement(id, cell.x, cell.y))
    }

    var sightRadius = 1
        set(value) {
            updateField("sightRadius", value, field)
            field = value
        }

    private fun updateField(field: String, newValue: Int, oldValue: Int) {
        if (newValue != oldValue)
            updates.add(StateChangeData(id, field, newValue.toString()))
    }

    fun collectResults(results: MutableCollection<Update>, visited: MutableCollection<String>) {
        results.addAll(updates)
        updates.clear()
        actors.filter { visited.add(it.id) }
                .forEach { it.collectResults(results, visited) }
    }

    fun set(key: PropertyLong, value: Long?) {
        val oldValue = properties.put(key, value)
        updates.add(StateChangeData(id, key.name, value.toString()))
    }

    fun get(key: PropertyLong): Long? {
        return properties[key]
    }

    override fun toString(): String {
        return "Actor(pos='$cell.x:$cell.y', name='$name', type=$type)"
    }
}
