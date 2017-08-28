package org.demoth.aworlds.server2;

import org.demoth.aworlds.server2.model.Location;
import org.demoth.aworlds.server2.model.Player;
import org.springframework.stereotype.Component;

@Component
public class ActorService {
    public Player loadCharacter(String charId) {
        Player player = new Player();
        return player;
    }

    public void setLocation(Player character) {
        character.setLocation(new Location());
    }

    public Player createCharacter(String[] params) {
        return null;
    }
}
