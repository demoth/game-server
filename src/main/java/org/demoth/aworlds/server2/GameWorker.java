package org.demoth.aworlds.server2;

import org.demoth.aworlds.server2.model.Location;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class GameWorker {

    ConcurrentLinkedQueue<Location> locations = new ConcurrentLinkedQueue<>();

    TaskScheduler scheduler;

    public void runLocation(Location location) {
        locations.add(location);
    }

    public void schedule() {
        for (Location location : locations) {
            scheduler.schedule(location::update, new Date());
        }
    }
}
