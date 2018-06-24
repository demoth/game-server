package org.demoth.gameserver.model

import org.demoth.gameserver.api.ActorType
import org.demoth.gameserver.api.PropertyLong
import org.demoth.gameserver.api.messaging.Movement
import org.demoth.gameserver.api.messaging.StateChangeData
import org.demoth.gameserver.api.messaging.Update
import java.util.*
import java.util.concurrent.atomic.AtomicLong

// todo make something better
object IdGenerator {
    private val current = AtomicLong()
    fun newUUID(): String {
        return current.incrementAndGet().toString()
    }
}

open class Actor(
        val type: ActorType,
        var name: String = "",
        x: Int = 0,
        y: Int = 0,
        val id: String = IdGenerator.newUUID(),
        var onUpdate: (() -> Unit)? = null,
        val properties: EnumMap<PropertyLong, Long> = EnumMap<PropertyLong, Long>(PropertyLong::class.java)) {

    // updates accumulated during current frame
    private val updates = ArrayList<Update>()

    // common properties
    lateinit var cell: Cell

    /**
     * Move actor to specific position. Network updates will be generated.
     * Used to move objects during game.
     */
    fun move(cell: Cell) {
        this.cell.actors.remove(this)
        this.cell = cell
        this.cell.actors.add(this)
        updates.add(Movement(id, cell.x, cell.y))
    }

    var sightRadius = 1
        set(value) {
            updateField("sightRadius", value, field)
            field = value
        }

    val actors: MutableList<Actor> = ArrayList()

    fun clearUpdates() {
        updates.clear()
    }

    private fun updateField(field: String, newValue: Int, oldValue: Int) {
        if (newValue != oldValue)
            updates.add(StateChangeData(id, field, newValue.toString()))
    }

    fun updateTree(visited: MutableCollection<String>) {
        onUpdate?.invoke()
        actors.filter { visited.add(it.id) }
                .forEach { it.updateTree(visited) }
    }

    protected fun collectResults(results: MutableCollection<Update>, visited: MutableCollection<String>) {
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
