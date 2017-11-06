package org.demoth.aworlds.server2;

import org.demoth.aworlds.server2.api.Message;
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
        cat.setLong(I_HEALTH, 100L);
        cat.setLong(I_MAX_HEALTH, 200L);
        cat.setLong(I_REGEN_HEALTH, 10L);
        cat.onUpdate = () -> {
            Long current = cat.getLong(I_HEALTH);
            Long max = cat.getLong(I_MAX_HEALTH);
            Long regen = cat.getLong(I_REGEN_HEALTH);
            if (current < max && regen > 0) {
                cat.setLong(I_HEALTH, min(max, current + regen));
            }
        };
        testLocation.add(cat);
        Collection<Message> results = testLocation.updateLocation();
        System.out.println(results);
        Assert.assertEquals(4, results.size());
        Assert.assertEquals(110L, cat.getLong(I_HEALTH).longValue());
    }

    @Test
    public void testUpdateInsideActor() {
        Location testLocation = new Location();
        Actor cat = new Actor();
        cat.setName("cat");
        cat.setLong(I_HEALTH, 100L);
        cat.setLong(I_MAX_HEALTH, 200L);
        cat.setLong(I_REGEN_HEALTH, 10L);
        cat.addActor(new Actor("hpregen", () -> {
            Long current = cat.getLong(I_HEALTH);
            Long max = cat.getLong(I_MAX_HEALTH);
            Long regen = cat.getLong(I_REGEN_HEALTH);
            if (current < max && regen > 0) {
                cat.setLong(I_HEALTH, min(max, current + regen));
            }
        }));
        testLocation.add(cat);
        Collection<Message> results = testLocation.updateLocation();
        System.out.println(results);
        Assert.assertEquals(4, results.size());
        Assert.assertEquals(110L, cat.getLong(I_HEALTH).longValue());
    }
}
