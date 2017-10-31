package org.demoth.aworlds.server2;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.demoth.aworlds.server2.api.Message;
import org.demoth.aworlds.server2.api.MessageType;
import org.demoth.aworlds.server2.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import sun.rmi.runtime.Log;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class UpdateSenderManager {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateSenderManager.class);

    final ObjectMapper maper;

    public UpdateSenderManager() {
        maper = new ObjectMapper();
    }

    private final Map<Player, Thread> players = new ConcurrentHashMap<>();

    public void startSendingUpdates(Player player) {
        Thread sender = new Thread(() -> {
            LOG.debug("Started sending updates for player {} ", player.getName());
            while (true) {
                try {
                    Message update = player.getUpdate();
                    TextMessage textMessage = update.toText(maper);
                    player.getSession().sendMessage(textMessage);
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
