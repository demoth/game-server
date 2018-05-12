package org.demoth.gameserver

import org.demoth.gameserver.api.ActorType
import org.demoth.gameserver.api.PropertyLong.HEALTH
import org.demoth.gameserver.api.messaging.*
import org.demoth.gameserver.model.Actor
import org.demoth.gameserver.model.Location
import org.demoth.gameserver.model.Player
import org.demoth.gameserver.model.createSampleLocation
import org.junit.Assert.fail
import org.junit.Test

class LocationTest {

    @Test(expected = IllegalStateException::class)
    fun `test empty board`() {
        Location(emptyArray())
    }

    @Test(expected = IllegalStateException::class)
    fun `test board with empty rows`() {
        Location(arrayOf(emptyArray(), emptyArray()))
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
        l.add(Actor(ActorType.CREATURE))
        assert(l.actors.size == 1)
        assert(l.board[0]!![0]!!.actors.size == 2)
    }

    @Test
    fun `test remove actor from board`() {
        val l = createSampleLocation()
        val actor = Actor(ActorType.CREATURE)
        l.add(actor)

        l.remove(actor)
        assert(l.actors[0].type == ActorType.CELL)
        assert(l.board[0]!![0]!!.actors.size == 1)
        assert(l.board[0]!![0]!!.actors.first().type == ActorType.FLOOR)
    }

    @Test
    fun `test update with emtpy actor`() {
        val l = createSampleLocation()
        l.add(Actor(ActorType.CREATURE))
        val updates = l.updateLocation()
        assert(updates.isEmpty())
    }

    @Test
    fun `test move actor`() {
        val l = createSampleLocation(width = 2)
        val actor = Actor(ActorType.CREATURE)
        l.add(actor)
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
        assert(stateChangeData.new_value == "1")
    }

    @Test
    fun `test update with actor with effect`() {
        val l = createSampleLocation()
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
        assert(stateChangeData.new_value == "2")
    }

    @Test(expected = IllegalStateException::class)
    fun `test add actor outside board`() {
        val l = createSampleLocation()
        val actor = Actor(ActorType.CREATURE)
        actor.move(2, actor.y)
        l.add(actor)
        fail("Should have not added actor!")
    }

    @Test
    fun `test add player`() {
        val l = createSampleLocation()
        l.add(Player())

        assert(l.actors.size == 1)
        assert(l.players.size == 1)
        assert(l.board[0]!![0]!!.actors.size == 2)
    }

    @Test
    fun `test add player add actor`() {
        val l = createSampleLocation(width = 3)
        val player = Player()
        l.add(player)
        l.add(Actor(ActorType.CREATURE, x = 1, y = 0))
        l.updateLocation()
        assert(player.results.size == 4) // two tiles and two actors
    }

    @Test
    fun `test add player add actor second update`() {
        val l = createSampleLocation(width = 2)
        val player = Player()
        l.add(player)
        l.add(Actor(ActorType.CREATURE, x = 1, y = 0))
        l.updateLocation()
        // emulate sending updates via network
        player.results.clear()

        l.updateLocation()
        assert(player.results.isEmpty())
    }

    @Test
    fun `test add player add actor remove actor`() {
        val l = createSampleLocation(width = 2)
        val player = Player()
        l.add(player)
        val actor = Actor(ActorType.CREATURE, x = 1, y = 0)
        l.add(actor)
        l.updateLocation()
        // emulate sending updates via network
        player.results.clear()

        l.remove(actor)

        l.updateLocation()
        assert(player.results.contains(DisappearData(actor.id)))
    }

    @Test
    fun `test add player add actor move out of view`() {
        val l = createSampleLocation(width = 3)
        val player = Player()
        l.add(player)
        val actor = Actor(ActorType.CREATURE, x = 1, y = 0)
        l.add(actor)
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
        val l = createSampleLocation(width = 3)
        val player = Player()
        l.add(player)
        val actor = Actor(ActorType.CREATURE, x = 2, y = 0)
        l.add(actor)
        l.updateLocation()
        assert(player.results.size == 3) // two tiles and player
        // emulate sending updates via network
        player.results.clear()

        // move left one cell
        l.move(actor, -1, 0)
        l.updateLocation()

        assert(player.results.contains(AppearData("CREATURE", actor.id, actor.x, actor.y)))
    }

    @Test
    fun `test player sees an actor appears`() {
        val l = createSampleLocation(width = 2)
        val player = Player()
        l.add(player)
        l.updateLocation()
        assert(player.results.size == 3) // two tiles and player
        // emulate sending updates via network
        player.results.clear()

        val actor = Actor(ActorType.CREATURE, x = 1, y = 0)
        l.add(actor)
        l.updateLocation()

        assert(player.results.contains(AppearData("CREATURE", actor.id, actor.x, actor.y)))
    }

    @Test
    fun `test player command move`() {
        val l = createSampleLocation(width = 2)
        val player = Player()
        l.add(player)
        player.enqueueRequest(MoveAction("e"))
        l.updateLocation()
        assert(player.x == 1)
        assert(!l.board[0]!![0]!!.actors.contains(player))
        assert(l.board[0]!![1]!!.actors.contains(player))
        assert(player.commands.isEmpty())
    }

    @Test
    fun `test player command move change visible area`() {
        val l = createSampleLocation(width = 4)
        val player = Player(1, 0)
        l.add(player)
        l.updateLocation()
        player.results.clear()

        player.enqueueRequest(MoveAction("e"))
        l.updateLocation()
        // (3,0) tile become visible
        assert(player.results.any { it is AppearData && it.x == 3 && it.y == 0 })
        // (0,0) tile become not visible
        assert(player.results.any { it is DisappearData && it.id == l.board[0]!![0]!!.actors.first().id })
    }

    @Test
    fun `test player command move out of board borders`() {
        val l = createSampleLocation()
        val player = Player()
        l.add(player)
        player.enqueueRequest(MoveAction("e"))
        l.updateLocation()
        // we expect nothing happens
        assert(player.x == 0)
        assert(l.board[0]!![0]!!.actors.contains(player))
        assert(player.commands.isEmpty())
    }

    @Test
    fun `test location with null cell`() {
        val l = Location(arrayOf(arrayOf<Actor?>(null)))
        l.updateLocation()
    }

    @Test
    fun `test player in location with null cell`() {
        val l = Location(arrayOf(arrayOf(Actor(ActorType.CELL), null)))
        val player = Player()
        l.add(player)
        l.updateLocation()
    }

    @Test
    fun `test player moves location with null cell`() {
        val l = Location(arrayOf(arrayOf(Actor(ActorType.CELL), null)))
        val player = Player()
        l.add(player)
        l.updateLocation()
        player.enqueueRequest(MoveAction("e"))
        l.updateLocation()
    }


}
