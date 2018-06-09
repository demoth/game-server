package org.demoth.gameserver

import org.demoth.gameserver.api.ActorType
import org.demoth.gameserver.generator.JavaRandom
import org.demoth.gameserver.generator.generateLocation
import org.demoth.gameserver.model.Player
import org.springframework.stereotype.Component

@Component
open class ActorService {
    fun loadCharacter(charId: String): Player {
        val player = Player()
        player.name = charId
        player.sightRadius = 2
        return player
    }

    fun setLocation(character: Player) {
        character.location = generateLocation(10, 10, JavaRandom())
        val cell = character.location?.actors?.find { it.type == ActorType.REGION }?.actors?.find { it.type == ActorType.CELL }
        if (cell != null) {
            character.location?.move(cell.x, cell.y)
        }
//        character.location = createSampleLocation(10, 10)
    }
}
