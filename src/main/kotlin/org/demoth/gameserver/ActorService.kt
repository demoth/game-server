package org.demoth.gameserver

import org.demoth.gameserver.api.ActorType
import org.demoth.gameserver.generator.JavaRandom
import org.demoth.gameserver.generator.generateLocation
import org.demoth.gameserver.model.Player
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
open class ActorService {
    companion object {
        val LOG = LoggerFactory.getLogger(ActorService::class.java)
    }

    fun loadCharacter(charId: String): Player {
        val player = Player()
        player.name = charId
        player.sightRadius = 2
        return player
    }

    fun setLocation(character: Player) {
        character.location = generateLocation(20, 20, JavaRandom())
        val cell = character.location!!.actors.find { it.type == ActorType.REGION }?.actors?.find { it.type == ActorType.CELL }
        if (cell != null) {
            LOG.debug("Player start position: ${cell.x}, ${cell.y}")
            character.place(cell.x, cell.y)
        }
    }
}
