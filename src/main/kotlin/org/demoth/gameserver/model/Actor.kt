package org.demoth.gameserver.model

import org.demoth.gameserver.api.ActorType
import org.demoth.gameserver.api.PropertyLong
import org.demoth.gameserver.api.messaging.Message
import org.demoth.gameserver.api.messaging.StateChangeData
import java.util.*

open class Actor(
        val type: ActorType,
        var name: String = "",
        var onUpdate: (() -> Unit)? = null) {

    private val longProps = EnumMap<PropertyLong, Long>(PropertyLong::class.java)

    // updates accumulated during current frame
    private val updates = ArrayList<Message>()

    // common properties
    val id: String = UUID.randomUUID().toString()

    var x: Int = 0
        set(value) {
            updateField("x", value)
            field = value
        }

    var y: Int = 0
        set(value) {
            updateField("y", value)
            field = value
        }

    var sightRadius = 1
        set(value) {
            updateField("sightRadius", value)
            field = value
        }

    val actors: MutableList<Actor> = ArrayList()

    private fun updateField(field: String, value: Int) {
        updates.add(StateChangeData(id, field, value.toString()))
    }

    internal fun updateTree(visited: MutableCollection<String>) {
        onUpdate?.invoke()
        actors.filter { visited.add(it.id) }
                .forEach { it.updateTree(visited) }
    }

    protected fun collectResults(results: MutableCollection<Message>, visited: MutableCollection<String>) {
        results.addAll(updates)
        updates.clear()
        actors.filter { visited.add(it.id) }
                .forEach { it.collectResults(results, visited) }
    }


    fun set(key: PropertyLong, value: Long?) {
        val oldValue = longProps.put(key, value)
        updates.add(StateChangeData(id, key.name, value.toString()))
    }

    fun get(key: PropertyLong): Long? {
        return longProps[key]
    }

    override fun toString(): String {
        return "Actor(type=$type, name='$name', id='$id')"
    }
}
