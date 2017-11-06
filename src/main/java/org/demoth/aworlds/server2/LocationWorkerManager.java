package org.demoth.aworlds.server2;

import org.demoth.aworlds.server2.api.Message;
import org.demoth.aworlds.server2.model.Location;
import org.demoth.aworlds.server2.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class LocationWorkerManager {

    private static final Logger LOG = LoggerFactory.getLogger(LocationWorkerManager.class);

    private final ConcurrentHashMap<Location, Thread> instances = new ConcurrentHashMap<>();

    public void runLocation(Location location) {
        Thread worker = new Thread(() -> {
            LOG.debug("Start updating {}", location.getName());
            while (true) {
                if (location.getPlayers().isEmpty()) {
                    LOG.debug("No players in {}, exiting...", location.getName());
                    return;
                }
                Collection<Message> updates = location.updateLocation();
                // todo move to other thread
                filterUpdates(updates, location.getPlayers());
                boolean allPlayersReady = location.getPlayers().stream().noneMatch(Player::idle);
                long sleep = location.getSleepTime(allPlayersReady);
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    LOG.debug("Finishing working thread: {}", Thread.currentThread().getName());
                    // todo persist location
                    return;
                }
            }
        });
        instances.put(location, worker);
        worker.setName("Location worker: " + location.getName());
        worker.start();
    }

    public void stopLocation(Location location) {
        Thread worker = instances.get(location);
        if (worker != null)
            worker.interrupt();
    }

    /*
     * For each player filter updates and put them to the send queue.
     */
    private void filterUpdates(Collection<Message> updates, Collection<Player> players) {
        // todo implement actual filtering
        updates.forEach(message ->
                players.forEach(player ->
                        player.enqueueResponse(message)));
    }
}
