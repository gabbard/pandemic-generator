package org.gabbard.pandemicgenerator

import org.junit.Assert.assertEquals
import org.junit.Test

class SeededColorTiebreakOrderTest {

    @Test
    fun isDeterministicForAGivenSeed() {
        assertEquals(seededColorTiebreakOrder(42L), seededColorTiebreakOrder(42L))
    }

    @Test
    fun containsAllFourColorsExactlyOnce() {
        val order = seededColorTiebreakOrder(1234L)
        assertEquals(Color.entries.toSet(), order.toSet())
        assertEquals(4, order.size)
    }

    @Test
    fun differentSeedsCanProduceDifferentOrderings() {
        val orderings = (0L until 20L).map { seededColorTiebreakOrder(it) }.toSet()
        assertEquals(true, orderings.size > 1)
    }
}
