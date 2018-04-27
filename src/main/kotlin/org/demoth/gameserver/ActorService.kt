package org.demoth.aworlds.server2

import org.demoth.aworlds.server2.api.LongPropertiesEnum
import org.demoth.aworlds.server2.model.Location
import org.demoth.aworlds.server2.model.Player
import org.springframework.stereotype.Component

@Component
open class ActorService {
    fun loadCharacter(charId: String): Player {
        val player = Player()
        player.name = charId
        player.type = "PLAYER"
        player.setLong(LongPropertiesEnum.SIGHT_RADIUS, 1L)
        return player
    }

    fun setLocation(character: Player) {
        character.location = Location()
        character.setLong(LongPropertiesEnum.X, 3L)
        character.setLong(LongPropertiesEnum.Y, 2L)
    }
}
