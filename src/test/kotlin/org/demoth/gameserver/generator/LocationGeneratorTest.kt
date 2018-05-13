package org.demoth.gameserver.generator

import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.util.*

@RunWith(JUnit4::class)
class LocationGeneratorTest {
    @Test
    fun testGenerator() {
        generateLocation(10, 20, Random(), 10, 4, 5, 2, 6)
    }
}