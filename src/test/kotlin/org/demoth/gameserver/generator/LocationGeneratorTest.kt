package org.demoth.gameserver.generator

import org.demoth.gameserver.api.ActorType
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class LocationGeneratorTest {

    @Test
    fun `test 1x1 location`() {
        val loc1x1 = generateLocation(1, 1, JavaRandom(), 1, 1, 1, 1, 1)
        assert(loc1x1.actors.size == 1)
        assert(loc1x1.actors.all { it.type == ActorType.REGION })
        assert(loc1x1.board[0]!![0]!!.type == ActorType.CELL, { "board should contain CELL type actors" })
        assert(loc1x1.actors.first().actors.first() === loc1x1.board.first()!!.first(), { "room should contain same cell as on the board" })
    }
}