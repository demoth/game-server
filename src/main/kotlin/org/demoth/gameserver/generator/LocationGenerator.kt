package org.demoth.gameserver.generator

import org.demoth.gameserver.api.ActorType
import org.demoth.gameserver.model.Actor
import org.demoth.gameserver.model.Location
import java.lang.Integer.min
import java.util.*
import java.util.Collections.shuffle
import java.util.stream.Collectors

fun generateLocation(width: Int, height: Int, r: Random,
                     roomTries: Int = 10,
                     roomWidthMin: Int = 3, roomWidthMax: Int = 7,
                     roomHeightMin: Int = 3, roomHeightMax: Int = 7): Location {
    val board = Array<Array<Actor?>?>(height, { arrayOfNulls(width) })
    val location = Location(board)
    for (i in 0 until roomTries) {
        generateRoom(location, r, roomWidthMin, roomWidthMax, roomHeightMin, roomHeightMax)
    }
    connectRegionsByTwoSets(location, r)
    return location
}

private fun generateRoom(location: Location, r: Random,
                         roomWidthMin: Int, roomWidthMax: Int,
                         roomHeightMin: Int, roomHeightMax: Int) {
    val roomWidth = min(location.board[0]!!.size, r.nextInt(roomWidthMax - roomWidthMin + 1) + roomWidthMin)
    val roomHeight = min(location.board.size, r.nextInt(roomHeightMax - roomHeightMin + 1) + roomHeightMin)

    val roomX = r.nextInt(location.board[0]!!.size - roomWidth + 1)
    val roomY = r.nextInt(location.board.size - roomHeight + 1)
    placeRoom(location, roomWidth, roomHeight, roomX, roomY)
}

fun placeRoom(location: Location, roomSizeX: Int, roomSizeZ: Int, roomX: Int, roomY: Int) {
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

private fun formRectangleRoom(board: Array<Array<Actor?>?>, width: Int, height: Int, roomX: Int, roomY: Int): Actor {
    val region = Actor(ActorType.REGION, "room")
    (0 until width).forEach { x ->
        (0 until height).forEach { y ->
            val cell = Actor(ActorType.CELL, "", roomX + x, roomY + y)
            region.actors.add(cell)
            board[roomY + y]!![roomX + x] = cell
        }
    }
    return region
}

fun connectRegionsByTwoSets(stage: Location, r: Random) {
    if (stage.actors.isEmpty())
        return
    // carve corridors to regions:
    val notConnected = LinkedList(stage.actors)
    val connected = LinkedList<Actor>()
    val region = stage.actors.first()
    notConnected.remove(region)
    connected.add(region)
    while (!notConnected.isEmpty()) {
        connectRegions(stage, r, notConnected, connected)
    }
    /*
    // walls, corners, etc
    stage.actors.forEach { room ->
        formRegionFragments(room, stage.cells(), stage.junctions())
    }
    */
}

fun connectRegions(stage: Location, r: Random, notConnected: MutableList<Actor>, connected: MutableList<Actor>) {
    if (!stageHasEmptyCells(stage)) {
        // pick random unconnected region, pick a border cell from it and make a door outwards
        val region = notConnected[r.nextInt(notConnected.size)]
        val junction = getAnyJunctionToDifferentRegion(region, r, connected, stage.board) ?: return
        // this is LITERALLY impossible (currently)
        stage.addJunction(junction.from().toPoint(), junction.to().toPoint(), junction)
        notConnected.remove(region)
        connected.add(region)
        return
    }
    // we carve a maze starting from any connected region
    val startingRegion = connected[r.nextInt(connected.size)]
    val maze = Actor(ActorType.REGION, "maze")
    var destinations = ArrayList<Actor>()
    val starting = getStartPosition(startingRegion, stage.cells(), r) ?: return
    var currentCell = starting!!.to()
    while (destinations.isEmpty() && currentCell != null) { // todo make protection from infinite loop
        currentCell!!.setType(CellType.MAZE)
        maze.cells().add(currentCell)
        currentCell!!.setRegion(maze)
        stage.cells()[currentCell!!.getZ()][currentCell!!.getX()] = currentCell
        destinations = findAdjacentRegions(currentCell!!, stage.cells(), notConnected)
        if (destinations.isEmpty()) {
            currentCell = getNextCell(currentCell!!, stage.cells(), stage.mazeStraightness(), r)
        }
    }

    // if a carved maze connects yet unconnected regions,
    // add some of them and newly created maze to connected and remove from notConnected.
    // currentCell will never be null here, but check is required
    if (destinations.size > 0 && currentCell != null) {
        // todo: make several connections
        val ending = destinations[0]
        stage.addJunction(starting!!.from().toPoint(), starting!!.to().toPoint(), starting)
        stage.addJunction(ending.from().toPoint(), ending.to().toPoint(), ending)
        notConnected.remove(ending.from().getRegion())
        connected.add(ending.from().getRegion())
        connected.add(maze)
        stage.actors.add(maze)
    } else {
        // maze was build but led nowhere
        // erase it from stageGrid
        for (c in maze.cells()) {
            stage.board[c.getZ()]!![c.getX()] = null
        }
    }
}

/**
 * pick any junction that will lead to another (connected) region
 */
fun getAnyJunctionToDifferentRegion(region: Actor, r: Random, connected: List<Actor>, board: Array<Array<Actor?>?>): Actor? {
    val shuffled = region.actors.filter { it.type == ActorType.CELL }
    shuffle(shuffled, r)
    for (cell in shuffled) {
        for (adjacent in getUpDownLeftRightCells(cell.x, cell.y).filter({ c -> c.insideStage(board) }).collect(Collectors.toList<Any>())) {
            val targetCell = board[adjacent.getZ()]!![adjacent.getX()]
            if (targetCell != null && targetCell.getRegion() != null && connected.contains(targetCell.getRegion())) {
                return Junction().from(cell).to(targetCell)
            }
        }
    }
    return null
}

fun getUpDownLeftRightCells(x: Int, y: Int): List<Actor> {
    val result = ArrayList<Actor>()
    val c = Actor(ActorType.CELL, x = x, y = y)
    result.add(up(c))
    result.add(down(c))
    result.add(left(c))
    result.add(right(c))
    return result
}

private fun up(c: Actor): Actor {
    return Actor(ActorType.CELL, x = c.x, y = c.y - 1, UP)
}

private fun left(c: Actor): Actor {
    return Actor(c.getX() - 1, c.getZ(), LEFT)
}

private fun down(c: Actor): Actor {
    return Actor(c.getX(), c.getZ() + 1, DOWN)
}

private fun right(c: Actor): Actor {
    return Actor(c.getX() + 1, c.getZ(), RIGHT)
}

fun stageHasEmptyCells(stage: Location): Boolean {
    for (row in stage.board)
        if (row!!.any { it == null })
            return true
    return false
}
