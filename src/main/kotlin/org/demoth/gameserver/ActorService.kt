package org.demoth.gameserver

import org.demoth.gameserver.model.Location
import org.demoth.gameserver.model.Player
import org.springframework.stereotype.Component

@Component
open class ActorService {
    fun loadCharacter(charId: String): Player {
        val player = Player()
        player.name = charId
        return player
    }

    fun setLocation(character: Player) {
        character.location = Location(arrayOf())
    }
}
