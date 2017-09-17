package org.demoth.aworlds.server2;

import org.demoth.aworlds.server2.model.Location;
import org.demoth.aworlds.server2.model.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class WorkManager {

    private static final Logger LOG = LoggerFactory.getLogger(WorkManager.class);

    ConcurrentHashMap<Location, Thread> instances = new ConcurrentHashMap<>();


    public void runLocation(Location location) {
        Thread worker = new Thread(() -> {
            LOG.debug("Start updating {}", location.getName());
            while (true) {
                boolean allPlayersReady = true;
                if (location.getPlayers().isEmpty()) {
                    LOG.debug("No players in {}, exiting...", location.getName());
                    return;
                }
                for (Player player : location.getPlayers()) {
                    if (player.idle()) {
                        allPlayersReady = false;
                        break;
                    }
                }
                location.update();
                long sleep = location.getSleepTime(allPlayersReady);
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    LOG.debug("Finishing working thread: {}", Thread.currentThread().getName());
                    return;
                }
            }
        });
        instances.put(location, worker);
        worker.setName("Location worker: " + location.getName());
        worker.start();
    }
}
