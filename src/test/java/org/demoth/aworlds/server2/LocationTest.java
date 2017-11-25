package org.demoth.aworlds.server2;

import org.demoth.aworlds.server2.api.messaging.MapLike;
import org.demoth.aworlds.server2.model.Actor;
import org.demoth.aworlds.server2.model.Location;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;

import static java.lang.Math.min;
import static org.demoth.aworlds.server2.api.LongPropertiesEnum.*;

public class LocationTest {

    @Test
    public void testSimpleLocation() {
        Location testLocation = new Location();
        Actor cat = new Actor();
        cat.setName("cat");
        cat.setLong(HEALTH, 100L);
        cat.setLong(MAX_HEALTH, 200L);
        cat.setLong(REGEN_HEALTH, 10L);
        cat.setLong(X, 10L);
        cat.setLong(Y, 20L);
        cat.onUpdate = () -> {
            Long current = cat.getLong(HEALTH);
            Long max = cat.getLong(MAX_HEALTH);
            Long regen = cat.getLong(REGEN_HEALTH);
            if (current < max && regen > 0) {
                cat.setLong(HEALTH, min(max, current + regen));
            }
        };
        testLocation.add(cat);
        Collection<MapLike> results = testLocation.updateLocation();
        System.out.println(results);
        Assert.assertEquals(7, results.size());
        Assert.assertEquals(110L, cat.getLong(HEALTH).longValue());
    }

    @Test
    public void testUpdateInsideActor() {
        Location testLocation = new Location();
        Actor cat = new Actor();
        cat.setName("cat");
        cat.setLong(HEALTH, 100L);
        cat.setLong(MAX_HEALTH, 200L);
        cat.setLong(REGEN_HEALTH, 10L);
        cat.setLong(X, 10L);
        cat.setLong(Y, 20L);
        cat.addActor(new Actor("hpregen", () -> {
            Long current = cat.getLong(HEALTH);
            Long max = cat.getLong(MAX_HEALTH);
            Long regen = cat.getLong(REGEN_HEALTH);
            if (current < max && regen > 0) {
                cat.setLong(HEALTH, min(max, current + regen));
            }
        }));
        testLocation.add(cat);
        Collection<MapLike> results = testLocation.updateLocation();
        System.out.println(results);
        Assert.assertEquals(7, results.size());
        Assert.assertEquals(110L, cat.getLong(HEALTH).longValue());
    }
}
