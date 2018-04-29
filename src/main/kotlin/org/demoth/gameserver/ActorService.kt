package org.demoth.gameserver

import org.demoth.gameserver.model.Player
import org.demoth.gameserver.model.createSampleLocation
import org.springframework.stereotype.Component

@Component
open class ActorService {
    fun loadCharacter(charId: String): Player {
        val player = Player()
        player.name = charId
        return player
    }

    fun setLocation(character: Player) {
        character.location = createSampleLocation(3, 3)
    }
}
