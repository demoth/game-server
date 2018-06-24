package org.demoth.gameserver

import org.demoth.gameserver.generator.LocationGeneratorTest
import org.demoth.gameserver.model.LocationTest
import org.junit.runner.RunWith
import org.junit.runners.Suite

@Suite.SuiteClasses(value = [
    LocationTest::class,
    LocationGeneratorTest::class,
    IntegrationTest::class
])
@RunWith(Suite::class)
class TestSuite
