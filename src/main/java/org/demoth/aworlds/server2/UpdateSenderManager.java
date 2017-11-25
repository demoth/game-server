package org.demoth.aworlds.server2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.demoth.aworlds.server2.api.messaging.MapLike;
import org.demoth.aworlds.server2.api.messaging.fromServer.UpdateMessage;
import org.demoth.aworlds.server2.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class UpdateSenderManager {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateSenderManager.class);

    final ObjectMapper mapper;
    private final Map<Player, Thread> players = new ConcurrentHashMap<>();

    public UpdateSenderManager() {
        mapper = new ObjectMapper();
    }

    public void startSendingUpdates(Player player) {
        Thread sender = new Thread(() -> {
            LOG.debug("Started sending updates for player {} ", player.getName());
            while (true) {
                try {
                    MapLike change = player.getUpdate();
                    UpdateMessage update = new UpdateMessage(Collections.singletonList(change));
                    player.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(update.toMap())));
                } catch (IOException e) {
                    LOG.error("Error while sending updates", e);
                    return;
                } catch (InterruptedException e) {
                    LOG.debug("Finished sending updates for {}", player.getName());
                    // todo persist player
                    return;
                }
            }
        });
        players.put(player, sender);
        sender.setName("Update sender for " + player.getName());
        sender.start();
    }

    public void stopSendingUpdates(Player player) {
        Thread sender = players.get(player);
        if (sender != null)
            sender.interrupt();
    }
}
