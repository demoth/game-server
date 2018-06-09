package org.demoth.gameserver.generator

import org.demoth.gameserver.api.ActorType.*
import org.demoth.gameserver.api.PropertyLong.DIRECTION
import org.demoth.gameserver.model.Actor
import org.demoth.gameserver.model.Board
import org.demoth.gameserver.model.Location
import java.lang.Integer.min
import java.util.*

fun generateLocation(width: Int, height: Int, r: RandomI,
                     roomTries: Int = 10,
                     roomWidthMin: Int = 3, roomWidthMax: Int = 7,
                     roomHeightMin: Int = 3, roomHeightMax: Int = 7,
                     mazeStraightness: Float = 0.1f): Location {
    val board = Board(height, { arrayOfNulls(width) })
    val location = Location(board)
    for (i in 0 until roomTries) {
        generateRoom(location, r, roomWidthMin, roomWidthMax, roomHeightMin, roomHeightMax)
    }
    connectRegionsByTwoSets(location, r, mazeStraightness)
    return location
}

private fun generateRoom(location: Location, r: RandomI,
                         roomWidthMin: Int, roomWidthMax: Int,
                         roomHeightMin: Int, roomHeightMax: Int) {
    val roomWidth = min(location.board[0]!!.size, r.nextInt(roomWidthMax - roomWidthMin + 1) + roomWidthMin)
    val roomHeight = min(location.board.size, r.nextInt(roomHeightMax - roomHeightMin + 1) + roomHeightMin)

    val roomX = r.nextInt(location.board[0]!!.size - roomWidth + 1)
    val roomY = r.nextInt(location.board.size - roomHeight + 1)
    placeRoom(location, roomWidth, roomHeight, roomX, roomY)
}

private fun placeRoom(location: Location, roomSizeX: Int, roomSizeZ: Int, roomX: Int, roomY: Int) {
    // check that new room does not overlap with existing ones
    for (y in 0 until roomSizeZ) {
        for (x in 0 until roomSizeX) {
            if (location.board[roomY + y]!![roomX + x] != null) {
                return
            }
        }
    }
    val region = formRectangleRoom(location.board, roomSizeX, roomSizeZ, roomX, roomY)
    location.actors.add(region)
}

private fun formRectangleRoom(board: Board, width: Int, height: Int, roomX: Int, roomY: Int): Actor {
    val region = Actor(REGION, "room")
    (0 until width).forEach { x ->
        (0 until height).forEach { y ->
            val cell = Actor(CELL, "", roomX + x, roomY + y)
            cell.actors.add(region)
            cell.actors.add(Actor(FLOOR, x = x, y = y))
            region.actors.add(cell)
            board[roomY + y]!![roomX + x] = cell
        }
    }
    return region
}

private fun connectRegionsByTwoSets(stage: Location, r: RandomI, mazeStraightness: Float) {
    if (stage.actors.isEmpty())
        return
    // carve corridors to regions:
    val notConnected = LinkedList(stage.actors)
    val connected = LinkedList<Actor>()
    val region = stage.actors.first()
    notConnected.remove(region)
    connected.add(region)
    while (!notConnected.isEmpty()) {
        connectRegions(stage, r, notConnected, connected, mazeStraightness)
    }
    /*
    // walls, corners, etc
    stage.actors.forEach { room ->
        formRegionFragments(room, stage.cells(), stage.junctions())
    }
    */
}

private fun connectRegions(location: Location, r: RandomI, notConnected: MutableList<Actor>, connected: MutableList<Actor>, mazeStraightness: Float) {
    if (!stageHasEmptyCells(location)) {
        // pick random unconnected region, pick a border cell from it and make a door outwards
        val region = notConnected[r.nextInt(notConnected.size)]
        // ?: return is very unlikely
        val junction = getAnyJunctionToDifferentRegion(region, r, connected, location.board) ?: return
        location.actors.add(junction)
        notConnected.remove(region)
        connected.add(region)
        return
    }
    val startingRegion = connected[r.nextInt(connected.size)]

    // sometimes we can connect two adjacent regions without any maze
    val junction = connectAdjacentRegions(startingRegion, notConnected, location.board, r)
    if (junction != null) {
        location.actors.add(junction)
        val connectedRegion = junction.actors[1].actors.find { it.type == REGION }!!
        notConnected.remove(connectedRegion)
        connected.add(connectedRegion)
        return
    }
    // we carve a maze starting from any connected region
    val maze = Actor(REGION, "maze")
    var destinations: List<Actor> = ArrayList()
    val startingGate = getStartPosition(startingRegion, location.board, r) ?: return
    var currentCell: Actor? = startingGate.actors[1] // fixme
    while (destinations.isEmpty() && currentCell != null) { // todo make protection from infinite loop
        //currentCell.setType(CellType.MAZE)
        maze.actors.add(currentCell)
        currentCell.actors.add(maze)
        location.board[currentCell.y]!![currentCell.x] = currentCell
        destinations = findAdjacentRegions(currentCell, location.board, notConnected)
        if (destinations.isEmpty()) {
            currentCell = getNextCell(currentCell, location.board, mazeStraightness, r)
        }
    }

    // if a carved maze connects yet unconnected regions,
    // add some of them and newly created maze to connected and remove from notConnected.
    // currentCell will never be null here, but check is required
    if (destinations.isNotEmpty() && currentCell != null) {
        // todo: make several connections
        val endingGate = destinations[0]
        location.actors.add(startingGate)
        location.actors.add(endingGate)
        notConnected.remove(endingGate.actors[1].actors.find { it.type == REGION })
        connected.add(endingGate.actors[1].actors.find { it.type == REGION }!!)
        connected.add(maze)
        location.actors.add(maze)
    } else {
        // maze was build but led nowhere
        // erase it from stageGrid
        for (c in maze.actors) {
            location.board[c.y]!![c.x] = null
        }
    }
}

fun connectAdjacentRegions(startingRegion: Actor, notConnected: List<Actor>, board: Board, r: RandomI): Actor? {
    val adjacent = startingRegion.actors
            .filter { it.type == CELL }
            .map { fromCell ->
                getUpDownLeftRightCells(fromCell.x, fromCell.y)
                        .filter { insideStage(it, board) }
                        .filter { board[it.y]?.get(it.x) != null }
                        .map { board[it.y]!![it.x]!! }
                        .map { it to it.actors.find { it.type == REGION } }
                        .filter { notConnected.contains(it.second) }
                        .map { fromCell to it.first }
            }.flatten().toMap()
    if (adjacent.isEmpty())
        return null
    else {
        val adjacentRegion = adjacent.entries.toList()[r.nextInt(adjacent.size)]
        val gate = Actor(GATE)
        gate.actors.add(adjacentRegion.key)
        gate.actors.add(adjacentRegion.value)
        return gate
    }
}

private fun getNextCell(currentCell: Actor, board: Board, mazeStraightness: Float, r: RandomI): Actor? {
    val adjacentAvailableCells = getAdjacentAvailableCells(currentCell.x, currentCell.y, board)
    if (adjacentAvailableCells.isEmpty()) {
        return null
    } else if (adjacentAvailableCells.size == 1) {
        return adjacentAvailableCells[0]
    }

    val oldDirCell = adjacentAvailableCells
            .filter { cell -> cell.get(DIRECTION) === currentCell.get(DIRECTION) }

    val changeDirection = r.nextFloat() > mazeStraightness
    if (!changeDirection && !oldDirCell.isEmpty()) {
        return oldDirCell[0]
    } else if (changeDirection) {
        adjacentAvailableCells.removeAll(oldDirCell)
    }

    return adjacentAvailableCells[r.nextInt(adjacentAvailableCells.size)]
}

private fun findAdjacentRegions(newCell: Actor, board: Board, notConnected: List<Actor>): List<Actor> {
    return getUpDownLeftRightCells(newCell.x, newCell.y)
            .filter { cell -> insideStage(cell, board) }
            .filter { cell -> board[cell.y]!![cell.x] != null } // there is a cell at the point (x,y)
            .filter { cell -> notConnected.contains(board[cell.y]!![cell.x]?.actors?.find { it.type == REGION }) } // connect only unconnected regions
            .map { cell ->
                Actor(GATE).apply {
                    actors.add(newCell)
                    actors.add(board[cell.y]!![cell.x]!!)
                }
            }
}

private fun getStartPosition(from: Actor, board: Board, r: RandomI): Actor? {
    val startingCell = findValidStartingCell(from, board, r) ?: return null
    val adjacentCells = getAdjacentAvailableCells(startingCell.x, startingCell.y, board)
    val targetCell = adjacentCells[r.nextInt(adjacentCells.size)]
    val gate = Actor(GATE)
    gate.actors.add(startingCell)
    gate.actors.add(targetCell)
    return gate
}

private fun findValidStartingCell(region: Actor, board: Board, r: RandomI): Actor? {

    val available = region.actors.filter { it.type == CELL }.filter { cell ->
        !getAdjacentAvailableCells(cell.x, cell.y, board).isEmpty()
    }.toList()
    if (available.isNotEmpty())
        return available[r.nextInt(available.size)]
    return null
}

private fun getAdjacentAvailableCells(x: Int, z: Int, board: Board): MutableList<Actor> {
    return getUpDownLeftRightCells(x, z)
            .filter { cell -> available(cell, board) }
            .toMutableList()
}

private fun available(cell: Actor, board: Board): Boolean {
    return insideStage(cell, board) && board[cell.y]!![cell.x] == null
}

/**
 * pick any junction that will lead to another (connected) region
 */
private fun getAnyJunctionToDifferentRegion(region: Actor, r: RandomI, connected: List<Actor>, board: Board): Actor? {
    val shuffled = region.actors.filter { it.type == CELL }.toMutableList()
    r.shuffle(shuffled)
    for (cell in shuffled) {
        for (adjacent in getUpDownLeftRightCells(cell.x, cell.y).filter({ c -> insideStage(c, board) })) {
            val targetCell = board[adjacent.y]!![adjacent.x]
            if (targetCell != null && connected.contains(targetCell.actors.find { it.type == REGION })) {
                val gate = Actor(GATE)
                gate.actors.add(cell)
                gate.actors.add(targetCell)
                return gate
            }
        }
    }
    return null
}

private fun insideStage(cell: Actor, board: Board): Boolean {
    return cell.y >= 0 && cell.y < board.size && cell.x >= 0 && cell.x < board[0]!!.size
}

private fun getUpDownLeftRightCells(x: Int, y: Int): List<Actor> {
    val result = ArrayList<Actor>()
    val c = Actor(CELL, x = x, y = y)
    result.add(up(c))
    result.add(down(c))
    result.add(left(c))
    result.add(right(c))
    return result
}

private fun up(c: Actor): Actor {
    val cell = Actor(CELL, x = c.x, y = c.y - 1)
    cell.set(DIRECTION, 1)
    return cell
}

private fun left(c: Actor): Actor {
    val cell = Actor(CELL, x = c.x - 1, y = c.y)
    cell.set(DIRECTION, 2)
    return cell
}

private fun down(c: Actor): Actor {
    val cell = Actor(CELL, x = c.x, y = c.y + 1)
    cell.set(DIRECTION, 3)
    return cell
}

private fun right(c: Actor): Actor {
    val cell = Actor(CELL, x = c.x + 1, y = c.y)
    cell.set(DIRECTION, 4)
    return cell
}

private fun stageHasEmptyCells(stage: Location): Boolean {
    for (row in stage.board)
        if (row!!.any { it == null })
            return true
    return false
}
