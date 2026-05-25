package org.gabbard.pandemicgenerator

import org.junit.Assert.assertEquals
import org.junit.Test

class CityStateTest {

    // ── valid construction ────────────────────────────────────────────────────

    @Test
    fun emptyInfectionsIsValid() {
        CityState(emptyMap()) // must not throw
    }

    @Test
    fun zeroCubesIsValid() {
        CityState(mapOf(Color.BLUE to 0))
    }

    @Test
    fun threeCubesIsValid() {
        CityState(mapOf(Color.BLUE to 3))
    }

    @Test
    fun multipleColorsAtMaxIsValid() {
        CityState(mapOf(Color.BLUE to 3, Color.RED to 3))
    }

    // ── invalid construction ──────────────────────────────────────────────────

    @Test(expected = IllegalArgumentException::class)
    fun fourCubesThrows() {
        CityState(mapOf(Color.BLUE to 4))
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeCubesThrows() {
        CityState(mapOf(Color.BLUE to -1))
    }

    @Test(expected = IllegalArgumentException::class)
    fun oneColorValidOneColorInvalidThrows() {
        CityState(mapOf(Color.BLUE to 2, Color.RED to 5))
    }

    // ── toString ──────────────────────────────────────────────────────────────

    @Test
    fun toStringDelegatesToInfectionsMap() {
        val infections = mapOf(Color.BLUE to 2)
        assertEquals(infections.toString(), CityState(infections).toString())
    }
}
