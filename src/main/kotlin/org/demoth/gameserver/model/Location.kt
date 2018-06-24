package org.demoth.gameserver.model

import org.demoth.gameserver.api.ActorType
import org.demoth.gameserver.api.messaging.*
import org.slf4j.LoggerFactory
import java.lang.Integer.max
import java.lang.Integer.min
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.function.Consumer
import java.util.stream.Stream
import kotlin.collections.HashSet

typealias Board = Array<Array<Cell?>?>

class Location(
        var board: Board,
        val regions: MutableCollection<Region> = mutableListOf(),
        val gates: MutableCollection<Gate> = mutableListOf()
) : Actor(ActorType.LOCATION) {

    companion object {
        val LOG = LoggerFactory.getLogger(Location::class.java)!!
    }

    // players are kept to manage connection
    val players = ConcurrentLinkedQueue<Player>()

    init {
        if (board.isEmpty() || board.all { it!!.isEmpty() })
            throw IllegalStateException("board must not be empty!")
        board.forEachIndexed { y, row ->
            row?.forEachIndexed { x, cell ->
                cell?.let { assert(it.x == x) }
                cell?.let { assert(it.y == y) }
                cell?.actors?.forEach {
                    it.cell = cell
                }
                //cell?.let { actors.add(it) }
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
                    appeared.add(AppearData(actor.type.toString(), actor.id, actor.cell.x, actor.cell.y))
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
        val left = max(0, player.cell.x - sightRadius)
        val up = max(0, player.cell.y - sightRadius)
        val right = min(board[0]!!.size - 1, player.cell.x + sightRadius)
        val down = min(board.size - 1, player.cell.y + sightRadius)
        (up..down).forEach { y ->
            (left..right).forEach { x ->
                board[y]!![x]?.actors?.let {
                    it.filter { it.type in setOf(ActorType.FLOOR, ActorType.CREATURE) }.forEach { result.add(it) }
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

                LOG.debug("executing: $command")
                LOG.debug("player pos before: ${player.cell.x}:${player.cell.y}")
                if (command is MoveAction) {
                    when (command.direction) {
                        "n" -> move(player, 0, -1)
                        "s" -> move(player, 0, 1)
                        "e" -> move(player, 1, 0)
                        "w" -> move(player, -1, 0)
                    }
                }
                LOG.debug("player pos after: ${player.cell.x}:${player.cell.y}")
            }
        }
    }

    fun move(actor: Actor, xDelta: Int, yDelta: Int) {
        val newX = actor.cell.x + xDelta
        val newY = actor.cell.y + yDelta
        if (newX < 0 || newY < 0
                || newX >= board[0]!!.size
                || newY >= board.size
                || board[newY]!![newX] == null)
            return
        // todo add movement check
        actor.move(board[newY]!![newX]!!)
    }

    fun add(actor: Actor, x: Int, y: Int) {
        if (actor is Player) {
            players.add(actor)
        }
        actors.add(actor)
        checkActorPosition(x, y)
        actor.cell = board[y]!![x]!!
        actor.cell.actors.add(actor)
    }

    private fun checkActorPosition(x: Int, y: Int) {
        if (y !in 0..(board.size - 1) || x !in 0..(board[0]!!.size - 1))
            throw IllegalStateException("Actor added outside board! Actor position : " +
                    "$x,$y, board size: ${board.firstOrNull()?.size},${board.size}")
    }

    fun remove(actor: Actor) {
        if (actor is Player) {
            players.remove(actor)
        }
        actors.remove(actor)
        checkActorPosition(actor.cell.x, actor.cell.y)
        board[actor.cell.y]!![actor.cell.x]!!.actors.remove(actor)
        // FIXME: actor still has a link to cell
    }

}

