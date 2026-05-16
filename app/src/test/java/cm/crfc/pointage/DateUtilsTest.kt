package cm.crfc.pointage

import cm.crfc.pointage.util.buildCityLine
import cm.crfc.pointage.util.calcMinutesLate
import cm.crfc.pointage.util.generateIntroText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DateUtilsTest {
    @Test
    fun `calcMinutesLate returns zero before reference time`() {
        assertEquals(0, calcMinutesLate("08:10"))
    }

    @Test
    fun `calcMinutesLate computes positive delta after reference time`() {
        assertEquals(15, calcMinutesLate("08:30"))
    }

    @Test
    fun `generateIntroText mentions provided date`() {
        val text = generateIntroText("2026-05-16")
        assertTrue(text.contains("16 mai 2026"))
    }

    @Test
    fun `buildCityLine formats expected city prefix`() {
        assertEquals("Yaounde, le 16 mai 2026", buildCityLine("2026-05-16"))
    }
}

