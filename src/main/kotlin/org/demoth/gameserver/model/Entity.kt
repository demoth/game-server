package org.demoth.gameserver.model

import java.util.concurrent.atomic.AtomicLong

// todo make something better
object IdGenerator {
    private val current = AtomicLong()
    fun newUUID(): String {
        return current.incrementAndGet().toString()
    }
}

abstract class Entity(
        var name: String = "",
        var id: String = IdGenerator.newUUID()
) {
    abstract fun update()
}