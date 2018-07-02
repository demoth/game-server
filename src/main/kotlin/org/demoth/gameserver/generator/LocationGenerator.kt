package org.demoth.gameserver.generator

import org.demoth.gameserver.api.ActorType.FLOOR
import org.demoth.gameserver.model.*
import java.lang.Integer.min
import java.util.*

private val empty: Region = Region()

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

    // FIXME: get rid of marker object
    empty.cells.clear()
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
    location.regions.add(region)
}

private fun formRectangleRoom(board: Board, width: Int, height: Int, roomX: Int, roomY: Int): Region {
    val room = Region()
    (0 until width).forEach { x ->
        (0 until height).forEach { y ->
            val cellX = roomX + x
            val cellY = roomY + y
            val cell = Cell(cellX, cellY, room, actors = mutableListOf(Actor(FLOOR)))
            room.cells.add(cell)
            cell.actors.forEach { it.cell = cell }
            board[cellY]!![cellX] = cell
        }
    }
    return room
}

private fun connectRegionsByTwoSets(stage: Location, r: RandomI, mazeStraightness: Float) {
    if (stage.regions.isEmpty())
        return
    // carve corridors to regions:
    val notConnected = LinkedList(stage.regions)
    val connected = LinkedList<Region>()
    val region = stage.regions.first()
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

private fun connectRegions(location: Location, r: RandomI, notConnected: MutableList<Region>, connected: MutableList<Region>, mazeStraightness: Float) {
    if (!stageHasEmptyCells(location)) {
        // pick random unconnected region, pick a border cell from it and make a door outwards
        val region = notConnected[r.nextInt(notConnected.size)]
        val junction = getAnyJunctionToDifferentRegion(region, r, connected, location.board) ?: return
        location.gates.add(junction)
        notConnected.remove(region)
        connected.add(region)
        return
    }
    val startingRegion = connected[r.nextInt(connected.size)]

    // sometimes we can connect two adjacent regions without any maze
    val junction = connectAdjacentRegions(startingRegion, notConnected, location.board, r)
    if (junction != null) {
        location.gates.add(junction)
        val connectedRegion = junction.to.region
        notConnected.remove(connectedRegion)
        connected.add(connectedRegion)
        return
    }
    // we carve a maze starting from any connected region
    val maze = Region()
    var destinations: List<Gate> = ArrayList()
    val startingGate = getStartPosition(startingRegion, location.board, r) ?: return
    var currentCell: Cell? = startingGate.to
    while (destinations.isEmpty() && currentCell != null) { // todo make protection from infinite loop
        maze.cells.add(currentCell)
        currentCell.region = maze
        val floor = Actor(FLOOR, "floor${currentCell.x}:${currentCell.y}")
        floor.cell = currentCell
        currentCell.actors.add(floor)
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
        val endingGate = destinations.first()
        location.gates.add(startingGate)
        location.gates.add(endingGate)
        notConnected.remove(endingGate.to.region)
        connected.add(endingGate.to.region)
        connected.add(maze)
        location.regions.add(maze)
    } else {
        // maze was build but led nowhere
        // erase it from stageGrid
        for (c in maze.cells) {
            location.board[c.y]!![c.x] = null
        }
    }
}

fun connectAdjacentRegions(startingRegion: Region, notConnected: List<Region>, board: Board, r: RandomI): Gate? {
    val adjacent = startingRegion.cells
            .map { fromCell ->
                getUpDownLeftRightCells(fromCell.x, fromCell.y)
                        .filter { insideStage(it, board) }
                        .filter { board[it.y]?.get(it.x) != null }
                        .map { board[it.y]!![it.x]!! }
                        .map { it to it.region }
                        .filter { notConnected.contains(it.second) }
                        .map { fromCell to it.first }
            }.flatten().toMap()
    if (adjacent.isEmpty())
        return null
    else {
        val adjacentRegion = adjacent.entries.toList()[r.nextInt(adjacent.size)]
        return Gate(adjacentRegion.key, adjacentRegion.value)
    }
}

private fun getNextCell(currentCell: Cell, board: Board, mazeStraightness: Float, r: RandomI): Cell? {
    val adjacentAvailableCells = getAdjacentAvailableCells(currentCell.x, currentCell.y, board)
    if (adjacentAvailableCells.isEmpty()) {
        return null
    } else if (adjacentAvailableCells.size == 1) {
        return adjacentAvailableCells[0]
    }

    val oldDirCell = adjacentAvailableCells
            .filter { cell -> cell.direction === currentCell.direction }

    val changeDirection = r.nextFloat() > mazeStraightness
    if (!changeDirection && !oldDirCell.isEmpty()) {
        return oldDirCell[0]
    } else if (changeDirection) {
        adjacentAvailableCells.removeAll(oldDirCell)
    }

    return adjacentAvailableCells[r.nextInt(adjacentAvailableCells.size)]
}

private fun findAdjacentRegions(newCell: Cell, board: Board, notConnected: List<Region>): List<Gate> {
    return getUpDownLeftRightCells(newCell.x, newCell.y)
            .filter { cell -> insideStage(cell, board) }
            .filter { cell -> board[cell.y]!![cell.x] != null } // there is a cell at the point (x,y)
            .filter { cell -> notConnected.contains(board[cell.y]!![cell.x]?.region) } // connect only unconnected regions
            .map { cell ->
                Gate(newCell, board[cell.y]!![cell.x]!!)
            }
}

private fun getStartPosition(from: Region, board: Board, r: RandomI): Gate? {
    val startingCell = findValidStartingCell(from, board, r) ?: return null
    val adjacentCells = getAdjacentAvailableCells(startingCell.x, startingCell.y, board)
    val targetCell = adjacentCells[r.nextInt(adjacentCells.size)]
    return Gate(startingCell, targetCell)
}

private fun findValidStartingCell(region: Region, board: Board, r: RandomI): Cell? {
    val available = region.cells.filter { cell ->
        !getAdjacentAvailableCells(cell.x, cell.y, board).isEmpty()
    }.toList()
    if (available.isNotEmpty())
        return available[r.nextInt(available.size)]
    return null
}

private fun getAdjacentAvailableCells(x: Int, z: Int, board: Board): MutableList<Cell> {
    return getUpDownLeftRightCells(x, z)
            .filter { cell -> available(cell, board) }
            .toMutableList()
}

private fun available(cell: Cell, board: Board): Boolean {
    return insideStage(cell, board) && board[cell.y]!![cell.x] == null
}

/**
 * pick any junction that will lead to another (connected) region
 */
private fun getAnyJunctionToDifferentRegion(region: Region, r: RandomI, connected: List<Region>, board: Board): Gate? {
    val shuffled = region.cells.toMutableList()
    r.shuffle(shuffled)
    for (cell in shuffled) {
        for (adjacent in getUpDownLeftRightCells(cell.x, cell.y).filter({ c -> insideStage(c, board) })) {
            val targetCell = board[adjacent.y]!![adjacent.x]
            if (targetCell != null && connected.contains(targetCell.region)) {
                return Gate(cell, targetCell)
            }
        }
    }
    return null
}

private fun insideStage(cell: Cell, board: Board): Boolean {
    return cell.y >= 0 && cell.y < board.size && cell.x >= 0 && cell.x < board[0]!!.size
}

private fun getUpDownLeftRightCells(x: Int, y: Int): List<Cell> {
    val result = ArrayList<Cell>()
    val c = Cell(x, y, empty)
    result.add(up(c))
    result.add(down(c))
    result.add(left(c))
    result.add(right(c))
    return result
}

private fun up(c: Cell): Cell {
    return Cell(c.x, c.y - 1, empty, direction = Direction.UP)
}

private fun left(c: Cell): Cell {
    return Cell(c.x - 1, c.y, empty, direction = Direction.LEFT)
}

private fun down(c: Cell): Cell {
    return Cell(c.x, c.y + 1, empty, direction = Direction.DOWN)
}

private fun right(c: Cell): Cell {
    return Cell(c.x + 1, c.y, empty, direction = Direction.RIGHT)
}

private fun stageHasEmptyCells(stage: Location): Boolean {
    for (row in stage.board)
        if (row!!.any { it == null })
            return true
    return false
}
