package org.demoth.gameserver.model

import org.demoth.gameserver.api.ActorType
import org.demoth.gameserver.api.messaging.*
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.function.Consumer
import java.util.stream.Stream
import kotlin.collections.HashSet

class Location(var board: Array<Array<Cell?>?>) : Actor(ActorType.LOCATION) {
    // players are kept to manage connection
    val players = ConcurrentLinkedQueue<Player>()

    init {
        board.forEachIndexed { y,  row ->
            row?.forEachIndexed {x,  cell ->
                cell?.actors?.forEach {
                    it.x = x
                    it.y = y
                    it.clearUpdates()
                    actors.add(it)
                }
            }
        }
    }

    fun updateLocation(): ArrayList<Message> {
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
            val sightLastFrame = HashSet<String>()
            val appeared = HashSet<AppearData>()
            sight.forEach { actor ->
                sightLastFrame.add(actor.id)
                if (!player.sightLastFrame.contains(actor.id)) {
                    // send appear data
                    appeared.add(AppearData(actor.type.toString(), actor.id, actor.x, actor.y))
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
        val result = HashSet<Actor>()
        val sightRadius = player.sightRadius
        val left = if (player.x - sightRadius < 0) 0 else player.x - sightRadius
        val up = if (player.y - sightRadius < 0) 0 else player.y - sightRadius
        val right = if (player.x + sightRadius > board[0]!!.size - 1) board[0]!!.size - 1 else player.x + sightRadius
        val down = if (player.y + sightRadius > board.size - 1) board.size - 1 else player.y + sightRadius
        (up..down).forEach { y ->
            (left..right).forEach { x ->
                result.addAll(board[y]!![x]!!.actors)
            }
        }
        return result
    }

    private fun performCommands() {
        players.forEach { player ->
            var performed = false
            while (true) {
                // execute player's commands until there are no commands,
                // or cannot do anything (like when paralyzed)
                var command: CommandMessage?
                if (player.commands.isEmpty())
                    break

                // todo implement real check
                if (performed)
                    break
                else {
                    command = player.commands.poll()
                    performed = true
                }

                println("executing: $command")
                val action = command.action
                if (action is MoveAction) {
                    when (action.direction) {
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
        player.x += x
        player.y += y
    }

    fun add(actor: Actor) {
        if (actor is Player) {
            players.add(actor)
        }
        if (actor.y !in 0..(board.size - 1) || actor.x !in 0..(board[0]!!.size - 1))
            throw IllegalStateException("Actor added outside board! Actor position : ${actor.x},${actor.y}, board size: ${board[0]?.size},${board.size}")
        board[actor.y]!![actor.x]!!.actors.add(actor)
        actors.add(actor)
    }

    fun remove(actor: Actor) {
        if (actor is Player) {
            players.remove(actor)
        }
        if (actor.y !in 0..(board.size - 1) || actor.x !in 0..(board[0]!!.size - 1))
            throw IllegalStateException("Actor removed outside board! Actor position : ${actor.x},${actor.y}, board size: ${board[0]?.size},${board.size}")
        board[actor.y]!![actor.x]!!.actors.remove(actor)
        actors.remove(actor)
    }

}