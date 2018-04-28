package org.demoth.gameserver

import org.demoth.gameserver.api.ActorType
import org.demoth.gameserver.api.PropertyLong.HEALTH
import org.demoth.gameserver.api.messaging.StateChangeData
import org.demoth.gameserver.model.Actor
import org.demoth.gameserver.model.Cell
import org.demoth.gameserver.model.Location
import org.demoth.gameserver.model.Player
import org.junit.Assert.fail
import org.junit.Test

class LocationTest {
    // #
    private fun create1TileLocation(): Location {
        val board = Array<Array<Cell?>?>(1, { arrayOf(Cell(Actor(ActorType.TILE))) })
        return Location(board)
    }

    // ##
    private fun create2TileLocation(): Location {
        val board = Array<Array<Cell?>?>(1, { arrayOf(Cell(Actor(ActorType.TILE)), Cell(Actor(ActorType.TILE))) })
        return Location(board)
    }

    @Test
    fun `test empty location update`() {
        val l = create1TileLocation()
        val updates = l.updateLocation()
        assert(updates.isEmpty())
    }

    @Test
    fun `test location add actor to board`() {
        val l = create1TileLocation()
        l.add(Actor(ActorType.CREATURE))
        assert(l.actors.size == 2)
        assert(l.board[0]?.get(0)?.actors?.size == 2)
    }

    @Test
    fun `test location remove actor from board`() {
        val l = create1TileLocation()
        val actor = Actor(ActorType.CREATURE)
        l.add(actor)

        l.remove(actor)
        assert(l.actors.size == 1)
        assert(l.actors[0].type == ActorType.TILE)
        assert(l.board[0]!![0]!!.actors.size == 1)
        assert(l.board[0]!![0]!!.actors.first().type == ActorType.TILE)
    }

    @Test
    fun `test location update with emtpy actor`() {
        val l = create1TileLocation()
        l.add(Actor(ActorType.CREATURE))
        val updates = l.updateLocation()
        assert(updates.isEmpty())
    }

    @Test
    fun `test location update with actor with update`() {
        val l = create1TileLocation()
        val felix = Actor(ActorType.CREATURE, "felix")
        felix.onUpdate = {
            felix.set(HEALTH, 1)
        }
        l.add(felix)
        val updates = l.updateLocation()
        assert(updates.size == 1)
        assert(updates[0] is StateChangeData)
        val stateChangeData = updates[0] as StateChangeData
        assert(stateChangeData.id == felix.id)
        assert(stateChangeData.field == "HEALTH")
        assert(stateChangeData.newValue == "1")
    }

    @Test
    fun `test location update with actor with effect`() {
        val l = create1TileLocation()
        val felix = Actor(ActorType.CREATURE, "felix")
        felix.actors.add(Actor(ActorType.EFFECT, "hpregen",
                onUpdate = {
                    felix.set(HEALTH, 2)
                }))
        l.add(felix)
        val updates = l.updateLocation()
        assert(updates.size == 1)
        assert(updates[0] is StateChangeData)
        val stateChangeData = updates[0] as StateChangeData
        assert(stateChangeData.id == felix.id)
        assert(stateChangeData.field == "HEALTH")
        assert(stateChangeData.newValue == "2")
    }

    @Test(expected = IllegalStateException::class)
    fun `test location add actor outside board`() {
        val l = create1TileLocation()
        val actor = Actor(ActorType.CREATURE)
        actor.x = 2
        l.add(actor)
        fail("Should have not added actor!")
    }

    @Test
    fun `test location add player`() {
        val l = create1TileLocation()
        l.add(Player())

        assert(l.actors.size == 2)
        assert(l.players.size == 1)

        assert(l.board[0]!![0]!!.actors.size == 2)
    }

    @Test
    fun `test location add player add actor`() {
        val l = create2TileLocation()
        val player = Player()
        l.add(player)
        l.add(Actor(ActorType.CREATURE, x = 1, y = 0))
        l.updateLocation()
        assert(player.results.size == 4) // two tiles and two actors
    }

    @Test
    fun `test location add player add actor second update`() {
        val l = create2TileLocation()
        val player = Player()
        l.add(player)
        l.add(Actor(ActorType.CREATURE, x = 1, y = 0))
        l.updateLocation()
        // emulate sending updates via network
        player.results.clear()

        l.updateLocation()
        assert(player.results.isEmpty())
    }

}
