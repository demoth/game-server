package org.demoth.aworlds.server2;

import org.demoth.aworlds.server2.api.LongPropertiesEnum;
import org.demoth.aworlds.server2.model.Location;
import org.demoth.aworlds.server2.model.Player;
import org.springframework.stereotype.Component;

@Component
public class ActorService {
    public Player loadCharacter(String charId) {
        Player player = new Player();
        player.setName(charId);
        player.setType("PLAYER");
        return player;
    }

    public void setLocation(Player character) {
        character.setLocation(new Location());
        character.setLong(LongPropertiesEnum.X, 4L);
        character.setLong(LongPropertiesEnum.Y, 7L);
    }

    public Player createCharacter(String[] params) {
        return null;
    }
}
