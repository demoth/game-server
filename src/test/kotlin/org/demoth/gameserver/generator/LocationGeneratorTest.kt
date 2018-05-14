package org.demoth.gameserver.generator

import org.demoth.gameserver.api.ActorType.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class LocationGeneratorTest {

    private fun createRandom(vararg objects: Any): RandomI {
        return object : RandomI {
            var index = 0
            override fun nextInt(bound: Int): Int {
                if (index >= objects.size)
                    throw IllegalStateException("Trying to get more objects than i have")
                val o = objects[index]
                when {
                    o !is Int -> throw IllegalStateException("Wrong object type: expected Int, actual: ${o::class}")
                    o > bound -> throw IllegalStateException("$o > bound($bound)")
                    else -> {
                        index++
                        return o
                    }
                }
            }

            override fun nextFloat(): Float {
                if (index >= objects.size)
                    throw IllegalStateException("Trying to get more objects than i have")
                val o = objects[index]
                when (o) {
                    !is Float -> throw IllegalStateException("Wrong object type: expected Float, actual: ${o::class}")
                    else -> {
                        index++
                        return o
                    }
                }
            }

            override fun shuffle(col: MutableList<*>) {
                println("Random.shuffle - doing nothing")
            }

        }
    }


    @Test
    fun `test 1x1 location`() {
        val loc1x1 = generateLocation(1, 1, JavaRandom(), 1, 1, 1, 1, 1)
        assert(loc1x1.actors.size == 1)
        assert(loc1x1.actors.all { it.type == REGION })
        assert(loc1x1.board[0]!![0]!!.type == CELL, { "board should contain CELL type actors" })
        assert(loc1x1.actors.first().actors.first() === loc1x1.board.first()!!.first(), { "room should contain same cell as on the board" })
    }

    @Test
    fun `test 2x1 location with two rooms`() {
        val r = createRandom(
                0, // room 1 w
                0, // room 1 h
                0, // room 1 x
                0, // room 1 y
                0, // room 2 w
                0, // room 2 h
                1, // room 2 x
                0, // room 2 y
                0) // room to start with
        val loc2x2 = generateLocation(2, 1, r, 2, 1, 1, 1, 1)
        assert(loc2x2.actors.count { it.type == REGION } == 2, { "should contain 2 rooms" })
        assert(loc2x2.actors.count { it.type == GATE } == 1, { "should contain 1 gate" })
        assert(loc2x2.actors.size == 3, { "should not contain anything else" })
    }

    @Test
    fun `test 3x1 location with two rooms and 1-cell-maze`() {
        val r = createRandom(
                0, // room 1 w
                0, // room 1 h
                0, // room 1 x
                0, // room 1 y
                0, // room 2 w
                0, // room 2 h
                2, // room 2 x
                0, // room 2 y
                0, // room to start with
                0, // cell to start mazing
                0f) // changing direction chance
        val loc3x1 = generateLocation(3, 1, r, 2, 1, 1, 1, 1)
        assert(loc3x1.actors.count { it.type == REGION } == 3, { "should contain 2 rooms and 1 maze" })
        assert(loc3x1.actors.count { it.type == GATE } == 2, { "should contain 2 gates" })
        assert(loc3x1.actors.size == 5, { "should not contain anything else" })
    }

    @Test
    fun `test 4x1 location with two rooms and 2-cell-maze`() {
        val r = createRandom(
                0, // room 1 w
                0, // room 1 h
                0, // room 1 x
                0, // room 1 y
                0, // room 2 w
                0, // room 2 h
                3, // room 2 x
                0, // room 2 y
                0, // room to start with
                0, // cell to start mazing
                0f) // changing direction chance
        val loc4x1 = generateLocation(4, 1, r, 2, 1, 1, 1, 1)
        assert(loc4x1.actors.count { it.type == REGION } == 3, { "should contain 2 rooms and 1 maze" })
        assert(loc4x1.actors.count { it.type == GATE } == 2, { "should contain 2 gates" })
        assert(loc4x1.actors.size == 5, { "should not contain anything else" })
    }


    @Test
    fun `test 1000x`() {
        (0..10000).forEach {
            val loc = generateLocation(10, 10, JavaRandom())
        }
    }
}