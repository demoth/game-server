package org.demoth.gameserver.model

import org.demoth.gameserver.api.ActorType
import org.demoth.gameserver.api.PropertyLong.HEALTH
import org.demoth.gameserver.api.messaging.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

private fun createSampleLocation(width: Int = 1, height: Int = 1): Location {
    val room = Region()
    return Location(Board(height) { y ->
        val row = mutableListOf<Cell>()
        (0 until width).forEach { x ->
            row.add(Cell(x, y, room, actors = mutableListOf(Actor(ActorType.FLOOR))))
        }
        row.toTypedArray()
    }, regions = mutableSetOf(room))
}

class LocationTest {

    @Test
    fun `test empty board`() {
        assertThrows<IllegalStateException> { Location(emptyArray()) }
    }

    @Test
    fun `test board with empty rows`() {
        assertThrows<IllegalStateException> { Location(arrayOf(emptyArray(), emptyArray())) }
    }

    @Test
    fun `test empty location update`() {
        val l = createSampleLocation()
        val updates = l.updateLocation()
        assert(updates.isEmpty())
    }

    @Test
    fun `test add actor to board`() {
        val l = createSampleLocation()
        val cat = Actor(ActorType.CREATURE)
        l.add(cat, 0, 0)
//        assert(l.actors.contains(cat), { "cat is not added" })
        assert(l.board[0]!![0]!!.actors.size == 2, { "cell should contain 2 actors: cat & floor" })
    }

    @Test
    fun `test remove actor from board`() {
        val l = createSampleLocation()
        val actor = Actor(ActorType.CREATURE)
        l.add(actor, 0, 0)

        l.remove(actor)
//        assert(l.actors.isEmpty())
        assert(l.board[0]!![0]!!.actors.size == 1)
        assert(l.board[0]!![0]!!.actors.first().type == ActorType.FLOOR)
    }

    @Test
    fun `test update with emtpy actor`() {
        val l = createSampleLocation()
        l.add(Actor(ActorType.CREATURE), 0, 0)
        val updates = l.updateLocation()
        assert(updates.isEmpty())
    }

    @Test
    fun `test move actor`() {
        val l = createSampleLocation(2)
        val actor = Actor(ActorType.CREATURE)
        l.add(actor, 0, 0)
        l.move(actor, 1, 0)
        val updates = l.updateLocation()
        // x is changed, y is not
        assert(updates.size == 1)
        assert(updates[0] == Movement(actor.id, 1, 0))

        // check that board is updated as well
        assert(l.board[0]!![1]!!.actors.contains(actor))
        assert(!l.board[0]!![0]!!.actors.contains(actor))
    }

    @Test
    fun `test update with actor with update`() {
        val l = createSampleLocation()
        val felix = Actor(ActorType.CREATURE)
        felix.onUpdate = {
            felix.set(HEALTH, 1)
        }
        l.add(felix, 0, 0)
        val updates = l.updateLocation()
        assert(updates.size == 1)
        assert(updates[0] is StateChangeData)
        val stateChangeData = updates[0] as StateChangeData
        assert(stateChangeData.id == felix.id)
        assert(stateChangeData.field == "HEALTH")
        assert(stateChangeData.new_value == "1")
    }

    @Test
    fun `test update with actor with effect`() {
        val l = createSampleLocation()
        val felix = Actor(ActorType.CREATURE)
        felix.actors.add(Actor(ActorType.EFFECT,
                onUpdate = {
                    felix.set(HEALTH, 2)
                }))
        l.add(felix, 0, 0)
        val updates = l.updateLocation()
        assert(updates.size == 1)
        assert(updates[0] is StateChangeData)
        val stateChangeData = updates[0] as StateChangeData
        assert(stateChangeData.id == felix.id)
        assert(stateChangeData.field == "HEALTH")
        assert(stateChangeData.new_value == "2")
    }

    @Test
    fun `test add actor outside board`() {
        val l = createSampleLocation()
        val actor = Actor(ActorType.CREATURE)
        l.add(actor, 0, 0)
        l.move(actor, 1, 1)
        assert(actor.cell?.x == 0, { "Actor should not move" })
        assert(actor.cell?.y == 0, { "Actor should not move" })
    }

    @Test
    fun `test add player`() {
        val l = createSampleLocation()
        l.add(Player(), 0, 0)

//        assert(l.actors.size == 1)
        assert(l.players.size == 1)
        assert(l.board[0]!![0]!!.actors.size == 2)
    }

    @Test
    fun `test add player add actor`() {
        val l = createSampleLocation(3)
        val player = Player()
        l.add(player, 0, 0)
        l.add(Actor(ActorType.CREATURE), 1, 0)
        l.updateLocation()
        assert(player.results.size == 4, { "Should have two tiles and two actors" })
    }

    @Test
    fun `test add player add actor second update`() {
        val l = createSampleLocation(2)
        val player = Player()
        l.add(player, 0, 0)
        l.add(Actor(ActorType.CREATURE), 1, 0)
        l.updateLocation()
        // emulate sending updates via network
        player.results.clear()

        l.updateLocation()
        assert(player.results.isEmpty())
    }

    @Test
    fun `test add player add actor remove actor`() {
        val l = createSampleLocation(2)
        val player = Player()
        l.add(player, 0, 0)
        val actor = Actor(ActorType.CREATURE)
        l.add(actor, 1, 0)
        l.updateLocation()
        // emulate sending updates via network
        player.results.clear()

        l.remove(actor)

        l.updateLocation()
        assert(player.results.contains(DisappearData(actor.id)))
    }

    @Test
    fun `test add player add actor move out of view`() {
        val l = createSampleLocation(3)
        val player = Player()
        l.add(player, 0, 0)
        val actor = Actor(ActorType.CREATURE)
        l.add(actor, 1, 0)
        l.updateLocation()
        // emulate sending updates via network
        player.results.clear()

        // move right one cell
        l.move(actor, 1, 0)
        l.updateLocation()

        assert(player.results.contains(Movement(actor.id, 2, 0)))
        assert(player.results.contains(DisappearData(actor.id)))
    }

    @Test
    fun `test player sees an actor come into view`() {
        val l = createSampleLocation(3)
        val player = Player()
        l.add(player, 0, 0)
        val actor = Actor(ActorType.CREATURE)
        l.add(actor, 2, 0)
        l.updateLocation()
        assert(player.results.size == 3, { "Should have two tiles and player" })
        // emulate sending updates via network
        player.results.clear()

        // move left one cell
        l.move(actor, -1, 0)
        l.updateLocation()
        val cell = actor.cell!!
        assert(player.results.contains(AppearData("CREATURE", actor.id, cell.x, cell.y)))
    }

    @Test
    fun `test player sees an actor appears`() {
        val l = createSampleLocation(2)
        val player = Player()
        l.add(player, 0, 0)
        l.updateLocation()
        assert(player.results.size == 3) // two tiles and player
        // emulate sending updates via network
        player.results.clear()

        val actor = Actor(ActorType.CREATURE)
        l.add(actor, 1, 0)
        l.updateLocation()

        val cell = actor.cell!!
        assert(player.results.contains(AppearData("CREATURE", actor.id, cell.x, cell.y)))
    }

    @Test
    fun `test player command move`() {
        val l = createSampleLocation(2)
        val player = Player()
        l.add(player, 0, 0)
        player.enqueueRequest(MoveAction("e"))
        l.updateLocation()
        assert(player.cell?.x == 1)
        assert(!l.board[0]!![0]!!.actors.contains(player))
        assert(l.board[0]!![1]!!.actors.contains(player))
        assert(player.commands.isEmpty())
    }

    @Test
    fun `test player command move change visible area`() {
        val l = createSampleLocation(4)
        val player = Player()
        l.add(player, 1, 0)
        l.updateLocation()
        player.results.clear()

        player.enqueueRequest(MoveAction("e"))
        l.updateLocation()
        assert(player.results.any { it is AppearData && it.x == 3 && it.y == 0 }, { "(3,0) tile should become visible" })
        assert(player.results.any { it is DisappearData && it.id == l.board[0]!![0]!!.actors.first().id }, { "(0,0) tile should become not visible" })
    }

    @Test
    fun `test player command move out of board borders`() {
        val l = createSampleLocation()
        val player = Player()
        l.add(player, 0, 0)
        player.enqueueRequest(MoveAction("e"))
        l.updateLocation()
        // we expect nothing happens
        assert(player.cell?.x == 0)
        assert(l.board[0]!![0]!!.actors.contains(player))
        assert(player.commands.isEmpty())
    }

    @Test
    fun `test location with null cell`() {
        val l = Location(arrayOf(arrayOf<Cell?>(null)))
        l.updateLocation()
    }

    @Test
    fun `test player in location with null cell`() {
        val l = Location(arrayOf(arrayOf(Cell(0, 0, Region()), null)))
        val player = Player()
        l.add(player, 0, 0)
        l.updateLocation()
    }

    @Test
    fun `test player moves location with null cell`() {
        val l = Location(arrayOf(arrayOf(Cell(0, 0, Region()), null)))
        val player = Player()
        l.add(player, 0, 0)
        l.updateLocation()
        player.enqueueRequest(MoveAction("e"))
        l.updateLocation()
    }


}
