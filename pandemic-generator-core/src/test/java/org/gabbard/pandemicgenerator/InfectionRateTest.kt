package org.gabbard.pandemicgenerator

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Random

class InfectionRateTest {

    // ── card draw counts per rate ─────────────────────────────────────────────

    @Test fun initialDrawsTwo()   = assertEquals(2, InfectionRate.INITIAL.numInfectionCardsToDraw)
    @Test fun stepTwoDrawsTwo()   = assertEquals(2, InfectionRate.STEP_TWO.numInfectionCardsToDraw)
    @Test fun stepThreeDrawsTwo() = assertEquals(2, InfectionRate.STEP_THREE.numInfectionCardsToDraw)
    @Test fun stepFourDrawsThree() = assertEquals(3, InfectionRate.STEP_FOUR.numInfectionCardsToDraw)
    @Test fun stepFiveDrawsThree() = assertEquals(3, InfectionRate.STEP_FIVE.numInfectionCardsToDraw)
    @Test fun stepSixDrawsFour()  = assertEquals(4, InfectionRate.STEP_SIX.numInfectionCardsToDraw)
    @Test fun stepSevenDrawsFour() = assertEquals(4, InfectionRate.STEP_SEVEN.numInfectionCardsToDraw)

    // ── integration: executeTransition draws the right number of cities ───────

    @Test
    fun infectTransitionDrawsCorrectCountForEveryRate() {
        val rng = Random(0)
        for (rate in InfectionRate.entries) {
            val state = stateAtRate(rate)
            val result = state.executeTransition(Transition.INFECT, rng)
                    as TrackableState.TransitionResult.Success.InfectionTransitionResult
            assertEquals(
                "Rate $rate should infect ${rate.numInfectionCardsToDraw} cities",
                rate.numInfectionCardsToDraw,
                result.infectedCities.size
            )
        }
    }

    // ── epidemic advances infection rate ──────────────────────────────────────

    @Test
    fun eachEpidemicAdvancesInfectionRateByOneStep() {
        val rng = Random(0)
        var state = stateAtRate(InfectionRate.INITIAL)
        val expectedProgression = listOf(
            InfectionRate.STEP_TWO,
            InfectionRate.STEP_THREE,
            InfectionRate.STEP_FOUR,
            InfectionRate.STEP_FIVE,
            InfectionRate.STEP_SIX,
            InfectionRate.STEP_SEVEN
        )
        for (expected in expectedProgression) {
            val epidemic = NamedEpidemic("E")
            state = state.copy(
                playerDeck = Deck(listOf(epidemic) + ALL_CITIES.take(4).map { CityPlayerCard(it) }),
                lastTransition = Transition.INFECT
            )
            val result = state.executeTransition(Transition.DRAW_PLAYER_CARDS, rng)
                    as TrackableState.TransitionResult.Success.DrawPlayerCardsTransitionResult
            state = result.newGameState
            assertEquals("After epidemic, infection rate should be $expected", expected, state.infectionRate)
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun stateAtRate(rate: InfectionRate): TrackableState {
        val players = listOf(Player(Role("Medic")), Player(Role("Scientist")))
        return TrackableState(
            curPlayer = 0,
            players = players,
            infectionDeck = Deck(ALL_CITIES.take(10).map { InfectionCard(it) }),
            infectionDiscardPile = ALL_CITIES.drop(10).map { InfectionCard(it) }.toSet(),
            playerDeck = Deck(ALL_CITIES.take(4).map { CityPlayerCard(it) }),
            infectionRate = rate,
            lastTransition = Transition.DRAW_PLAYER_CARDS
        )
    }
}
