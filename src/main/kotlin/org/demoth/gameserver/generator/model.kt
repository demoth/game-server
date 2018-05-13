package org.demoth.gameserver.generator

import java.util.*

interface RandomI {
    fun nextInt(bound: Int): Int
    fun nextFloat(): Float
    fun shuffle(col: MutableList<*>)
}

class JavaRandom : RandomI {
    private val r = Random()

    override fun shuffle(col: MutableList<*>) {
        col.shuffle(r)
    }

    override fun nextInt(bound: Int): Int {
        return r.nextInt(bound)
    }

    override fun nextFloat(): Float {
        return r.nextFloat()
    }
}