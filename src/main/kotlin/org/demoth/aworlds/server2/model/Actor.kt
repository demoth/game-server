package org.demoth.aworlds.server2.model

import org.demoth.aworlds.server2.api.LongPropertiesEnum
import org.demoth.aworlds.server2.api.messaging.Message
import org.demoth.aworlds.server2.api.messaging.StateChangeData
import java.util.*

open class Actor() {
    var onUpdate: (() -> Unit)? = null
    var name: String? = null
    private val longProps = EnumMap<LongPropertiesEnum, Long>(LongPropertiesEnum::class.java)

    // updates accumulated during current frame
    private val updates = ArrayList<Message>()
    var type: String? = null
    val id: String = UUID.randomUUID().toString()
    val actors: MutableList<Actor> = ArrayList()

    constructor(name: String, update: (() -> Unit)?) : this() {
        this.name = name
        onUpdate = update
    }

    fun getActors(): MutableCollection<Actor> {
        return actors
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

    override fun toString(): String {
        return "Actor{" +
                "name='" + name + '\''.toString() +
                ", id='" + id + '\''.toString() +
                '}'.toString()
    }

    fun setLong(key: LongPropertiesEnum, value: Long?) {
        val oldValue = longProps.put(key, value)
        updates.add(StateChangeData(id, key.name, value.toString()))
    }

    fun getLong(key: LongPropertiesEnum): Long? {
        return longProps[key]
    }

    fun addActor(a: Actor) {
        getActors().add(a)
    }
}
