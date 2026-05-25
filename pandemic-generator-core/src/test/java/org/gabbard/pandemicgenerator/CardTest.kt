package org.gabbard.pandemicgenerator

import org.junit.Assert.assertEquals
import org.junit.Test

class CardTest {

    private val city = City("London", Color.BLUE)

    // ── CityPlayerCard ────────────────────────────────────────────────────────

    @Test
    fun cityPlayerCardUserStringReturnsCityName() {
        assertEquals("London", CityPlayerCard(city).userString)
    }

    @Test
    fun cityPlayerCardToStringReturnsCityName() {
        assertEquals("London", CityPlayerCard(city).toString())
    }

    // ── InfectionCard ─────────────────────────────────────────────────────────

    @Test
    fun infectionCardUserStringReturnsCityName() {
        assertEquals("London", InfectionCard(city).userString)
    }

    @Test
    fun infectionCardToStringReturnsCityName() {
        assertEquals("London", InfectionCard(city).toString())
    }

    // ── SimpleEpidemic ────────────────────────────────────────────────────────

    @Test
    fun simpleEpidemicUserStringIsEpidemic() {
        assertEquals("Epidemic", SimpleEpidemic().userString)
    }

    @Test
    fun simpleEpidemicToStringIsEpidemic() {
        assertEquals("Epidemic", SimpleEpidemic().toString())
    }

    // ── NamedEpidemic ─────────────────────────────────────────────────────────

    @Test
    fun namedEpidemicUserStringReturnsName() {
        assertEquals("Slippery Slope", NamedEpidemic("Slippery Slope").userString)
    }

    @Test
    fun namedEpidemicToStringReturnsName() {
        assertEquals("Slippery Slope", NamedEpidemic("Slippery Slope").toString())
    }

    // ── EventCard ─────────────────────────────────────────────────────────────

    @Test
    fun eventCardUserStringReturnsName() {
        assertEquals("Airlift", EventCard("Airlift").userString)
    }

    @Test
    fun eventCardToStringReturnsName() {
        assertEquals("Airlift", EventCard("Airlift").toString())
    }
}
