package org.demoth.gameserver.model

import org.demoth.gameserver.api.ActorType
import org.demoth.gameserver.api.messaging.*
import java.lang.Integer.max
import java.lang.Integer.min
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.function.Consumer
import java.util.stream.Stream
import kotlin.collections.HashSet

typealias Board = Array<Array<Actor?>?>

/*
    Location.actors are basically cells, that contain other actors
 */
class Location(var board: Board) : Actor(ActorType.LOCATION) {
    // players are kept to manage connection
    val players = ConcurrentLinkedQueue<Player>()

    init {
        if (board.isEmpty() || board.all { it!!.isEmpty() })
            throw IllegalStateException("board must not be empty!")
        board.forEachIndexed { y, row ->
            row?.forEachIndexed { x, cell ->
                cell?.actors?.forEach {
                    it.move(x, y)
                    it.clearUpdates()
                }
                cell?.let { actors.add(it) }
            }
        }
    }

    fun updateLocation(): List<Update> {
        val result = ArrayList<Update>()
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

    private fun filterUpdates(player: Player, result: Collection<Update>): Stream<Update> {
        // todo: add hook to encapsulate filterUpdates() game logic
        return result.stream()
    }

    private fun getPlayerSight(player: Player, board: Board): Set<Actor> {
        // todo: add hook to encapsulate getPlayerSight() game logic
        val result = HashSet<Actor>()
        val sightRadius = player.sightRadius
        val left = max(0, player.x - sightRadius)
        val up = max(0, player.y - sightRadius)
        val right = min(board[0]!!.size - 1, player.x + sightRadius)
        val down = min(board.size - 1, player.y + sightRadius)
        (up..down).forEach { y ->
            (left..right).forEach { x ->
                board[y]!![x]?.actors?.let {
                    result.addAll(it)
                }
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
                var command: Message?
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
                println("player pos before: ${player.x}:${player.y}")
                if (command is MoveAction) {
                    when (command.direction) {
                        "n" -> move(player, 0, -1)
                        "s" -> move(player, 0, 1)
                        "e" -> move(player, 1, 0)
                        "w" -> move(player, -1, 0)
                    }
                }
                println("player pos after: ${player.x}:${player.y}")
            }
        }
    }

    fun move(actor: Actor, x: Int, y: Int) {
        val newX = actor.x + x
        val newY = actor.y + y
        if (newX < 0 || newY < 0
                || newX >= board[0]!!.size
                || newY >= board.size
                || board[newY]!![newX] == null)
            return
        board[actor.y]!![actor.x]!!.actors.remove(actor)
        actor.move(newX, newY)
        checkActorPosition(actor)
        board[actor.y]!![actor.x]!!.actors.add(actor)
    }

    fun add(actor: Actor) {
        if (actor is Player) {
            players.add(actor)
        }
        checkActorPosition(actor)
        board[actor.y]!![actor.x]!!.actors.add(actor)
    }

    private fun checkActorPosition(actor: Actor) {
        if (actor.y !in 0..(board.size - 1) || actor.x !in 0..(board[0]!!.size - 1))
            throw IllegalStateException("Actor added outside board! Actor position : " +
                    "${actor.x},${actor.y}, board size: ${board.firstOrNull()?.size},${board.size}")
    }

    fun remove(actor: Actor) {
        if (actor is Player) {
            players.remove(actor)
        }
        checkActorPosition(actor)
        board[actor.y]!![actor.x]!!.actors.remove(actor)
    }

}

fun createSampleLocation(width: Int = 1, height: Int = 1): Location {
    return Location(Board(height, {
        val row = mutableListOf<Actor>()
        (0 until width).forEach {
            val cell = Actor(ActorType.CELL)
            cell.actors.add(Actor(ActorType.FLOOR))
            row.add(cell)
        }
        row.toTypedArray()
    }))
}