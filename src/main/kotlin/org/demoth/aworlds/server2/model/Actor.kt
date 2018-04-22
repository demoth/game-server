package org.demoth.aworlds.server2.model

import org.demoth.aworlds.server2.api.LongPropertiesEnum
import org.demoth.aworlds.server2.api.messaging.Message
import org.demoth.aworlds.server2.api.messaging.fromServer.StateChangeData

import java.util.ArrayList
import java.util.EnumMap
import java.util.UUID

open class Actor() {
    var onUpdate: Callback? = null
    var name: String? = null
    private val longProps = EnumMap<LongPropertiesEnum, Long>(LongPropertiesEnum::class.java)

    // updates accumulated during current frame
    private val updates = ArrayList<Message>()
    var type: String? = null
    val id: String
    val actors: MutableList<Actor>

    constructor(name: String, update: Callback) : this() {
        this.name = name
        onUpdate = update
    }

    init {
        // todo use safe generator
        id = UUID.randomUUID().toString()
        actors = ArrayList()
    }

    fun getActors(): MutableCollection<Actor> {
        return actors
    }

    internal fun updateTree(visited: MutableCollection<String>) {
        if (onUpdate != null) {
            onUpdate!!.run()
        }
        for (actor in actors) {
            if (visited.add(actor.id))
                actor.updateTree(visited)
        }
    }

    protected fun collectResults(results: MutableCollection<Message>, visited: MutableCollection<String>) {
        results.addAll(updates)
        updates.clear()
        for (actor in actors) {
            if (visited.add(actor.id))
                actor.collectResults(results, visited)
        }
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
