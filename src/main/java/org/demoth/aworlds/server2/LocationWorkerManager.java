package org.demoth.aworlds.server2;

import org.demoth.aworlds.server2.api.LongPropertiesEnum;
import org.demoth.aworlds.server2.api.messaging.Message;
import org.demoth.aworlds.server2.api.messaging.fromServer.Positioned;
import org.demoth.aworlds.server2.api.messaging.fromServer.StateChangeData;
import org.demoth.aworlds.server2.model.Location;
import org.demoth.aworlds.server2.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import static org.demoth.aworlds.server2.api.LongPropertiesEnum.X;
import static org.demoth.aworlds.server2.api.LongPropertiesEnum.Y;

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
                // todo: make configurable
                long sleep = (long) (allPlayersReady ? 100 : 2000);
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
        players.forEach(player -> {
            Long radius = player.getLong(LongPropertiesEnum.SIGHT_RADIUS);
            updates.stream().filter(message -> {
                        if (message instanceof StateChangeData) {
                            StateChangeData data = (StateChangeData) message;
                            return player.getId().equals(data.id);
                        }
                        if (message instanceof Positioned) {
                            Positioned appear = (Positioned) message;
                            if (Math.abs(appear.getX() - player.getLong(X)) > radius)
                                return false;
                            if (Math.abs(appear.getY() - player.getLong(Y)) > radius)
                                return false;
                            return true;
                        }
                return false;
                    }
            ).forEach(player::enqueueResponse);
        });
    }
}
