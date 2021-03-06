package org.demoth.gameserver

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
        player.sightRadius = 5
        return player
    }

    fun setLocation(character: Player) {
        character.location = generateLocation(30, 30, JavaRandom())
        LOG.info("Location loaded {}", character.location!!.id)
        val cell = character.location!!.regions.first().cells.first()
        LOG.debug("Player start position: $cell")
        character.location!!.add(character, cell.x, cell.y)
    }
}
