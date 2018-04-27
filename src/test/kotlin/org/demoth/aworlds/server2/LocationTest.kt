package org.demoth.aworlds.server2

import org.demoth.aworlds.server2.api.LongPropertiesEnum.*
import org.demoth.aworlds.server2.model.Actor
import org.demoth.aworlds.server2.model.Location
import org.junit.Assert
import org.junit.Test
import java.lang.Math.min

class LocationTest {

    @Test
    fun testSimpleLocation() {
        val testLocation = Location()
        val cat = Actor()
        cat.name = "cat"
        cat.setLong(HEALTH, 100L)
        cat.setLong(MAX_HEALTH, 200L)
        cat.setLong(REGEN_HEALTH, 10L)
        cat.setLong(X, 10L)
        cat.setLong(Y, 20L)
        cat.onUpdate = {
            val current = cat.getLong(HEALTH)
            val max = cat.getLong(MAX_HEALTH)
            val regen = cat.getLong(REGEN_HEALTH)
            if (current != null && max != null && regen != null)
                if (current < max && regen > 0) {
                    cat.setLong(HEALTH, min(max, current + regen))
                }

        }
        testLocation.add(cat)
        val results = testLocation.updateLocation()
        println(results)
        Assert.assertEquals(7, results.size.toLong())
        Assert.assertEquals(110L, cat.getLong(HEALTH)!!.toLong())
    }

    @Test
    fun testUpdateInsideActor() {
        val testLocation = Location()
        val cat = Actor()
        cat.name = "cat"
        cat.setLong(HEALTH, 100L)
        cat.setLong(MAX_HEALTH, 200L)
        cat.setLong(REGEN_HEALTH, 10L)
        cat.setLong(X, 10L)
        cat.setLong(Y, 20L)
        cat.addActor(Actor("hpregen", {
            val current = cat.getLong(HEALTH)
            val max = cat.getLong(MAX_HEALTH)
            val regen = cat.getLong(REGEN_HEALTH)
            if (current != null && max != null && regen != null)
                if (current < max && regen > 0) {
                    cat.setLong(HEALTH, min(max, current + regen))
                }

        }))
        testLocation.add(cat)
        val results = testLocation.updateLocation()
        println(results)
        Assert.assertEquals(7, results.size.toLong())
        Assert.assertEquals(110L, cat.getLong(HEALTH)!!.toLong())
    }
}
