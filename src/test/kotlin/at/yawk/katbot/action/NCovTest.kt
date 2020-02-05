package at.yawk.katbot.action

import org.testng.Assert
import org.testng.annotations.Test

class NCovTest {
    @Test
    fun testLoad() {
        val regions = NCov.load()
        Assert.assertTrue(regions.size >= 22)
        for (region in regions) {
            Assert.assertTrue(region.deaths <= region.cases)
            Assert.assertTrue(region.recoveries <= region.cases)
        }
        val china = regions.first { it.name.contains("China") }
        Assert.assertTrue(china.cases >= 24348 )
        Assert.assertTrue(china.deaths >= 491)
        Assert.assertTrue(china.recoveries >= 892)
    }
}