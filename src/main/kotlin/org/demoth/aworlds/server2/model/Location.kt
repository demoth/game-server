package org.demoth.aworlds.server2.model

import org.demoth.aworlds.server2.api.LongPropertiesEnum.*
import org.demoth.aworlds.server2.api.messaging.Message
import org.demoth.aworlds.server2.api.messaging.CommandMessage
import org.demoth.aworlds.server2.api.messaging.MoveAction
import org.demoth.aworlds.server2.api.messaging.AppearData
import org.demoth.aworlds.server2.api.messaging.DisappearData
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.function.Consumer
import java.util.stream.Stream

class Location : Actor() {
    // players are kept to manage connection
    val players = ConcurrentLinkedQueue<Player>()
    var board: Array<Array<Cell?>?>

    init {
        // todo remove
        val size = 6
        name = "Test location"
        val location = arrayOfNulls<CharArray>(size)
        board = arrayOfNulls(size)
        location[0] = "######".toCharArray()
        location[1] = "#...##".toCharArray()
        location[2] = ".....#".toCharArray()
        location[3] = ".....#".toCharArray()
        location[4] = "#.#...".toCharArray()
        location[5] = "######".toCharArray()
        for (y in 0 until size) {
            board[y] = arrayOfNulls(size)
            val chars = location[y]
            for (x in 0 until size) {
                val c = chars!![x]
                val floorTile = Actor()
                floorTile.setLong(X, x.toLong())
                floorTile.setLong(Y, y.toLong())
                floorTile.type = if (c == '#') "WALL" else "FLOOR"
                actors.add(floorTile)
                board[y]!![x] = Cell(floorTile)
            }
        }
    }

    fun getPlayers(): Collection<Player> {
        return players
    }

    fun updateLocation() : ArrayList<Message> {
        val result = ArrayList<Message>()
        // invoke onUpdate() callback on the whole tree
        updateTree(TreeSet())
        // process requests for actors
        performCommands()
        // get the results of above updates,
        // there only are state changes (for now).
        // all appear/disappear data is filled below
        collectResults(result, TreeSet())
        // for each player:
        // 1 get player sight (visible objects)
        // 2 compare with player.sightLastFrame (calculate appear/disappear)
        // 3 for disappeared objects in sight sent disappear data
        // 4 for appeared object in sight send appear data
        players.forEach { player ->
            filterUpdates(player, result).forEach({ player.enqueueResponse(it) })

            val sight = getPlayerSight(player, board)
            val sightLastFrame = TreeSet<String>()
            val appeared = TreeSet<AppearData>()
            for (actor in sight) {
                sightLastFrame.add(actor.id)
                if (!player.sightLastFrame.contains(actor.id)) {
                    // send appear data
                    appeared.add(AppearData(actor.type!!, id, actor.getLong(X)!!, actor.getLong(Y)!!))
                } else {
                    // do not send disappear data
                    player.sightLastFrame.remove(actor.id)
                }
            }
            appeared.forEach(Consumer<AppearData> { player.enqueueResponse(it) })
            player.sightLastFrame.forEach { id -> player.enqueueResponse(DisappearData(id)) }
            player.sightLastFrame = sightLastFrame
        }
        return result
    }

    private fun filterUpdates(player: Player, result: Collection<Message>): Stream<Message> {
        // todo: add hook to encapsulate filterUpdates() game logic
        return result.stream()
    }

    private fun getPlayerSight(player: Player, board: Array<Array<Cell?>?>): Set<Actor> {
        // todo: add hook to encapsulate getPlayerSight() game logic
        val actors = TreeSet<Actor>()
        val sightRadius = player.getLong(SIGHT_RADIUS)

        return actors
    }

    private fun performCommands() {
        for (player in players) {
            var performed = false
            while (true) {
                var command: CommandMessage? = player.commands.peek() ?: break

                // todo implement real check
                if (performed)
                    break
                else {
                    command = player.commands.poll()
                    performed = true
                }

                println("executing: " + command!!)
                if (command.action is MoveAction) {
                    val move = command.action as MoveAction
                    when (move.direction) {
                        "n" -> move(player, -1, 0)
                        "s" -> move(player, 1, 0)
                        "e" -> move(player, 0, 1)
                        "w" -> move(player, 0, -1)
                    }
                }
            }
        }
    }

    private fun move(player: Player, y: Int, x: Int) {
        player.setLong(X, player.getLong(X)!! + x)
        player.setLong(Y, player.getLong(Y)!! + y)

    }

    fun add(actor: Actor) {
        if (actor is Player) {
            players.add(actor)
        }
        actors.add(actor)
    }

    fun removePlayer(player: Player) {
        players.remove(player)
    }
}