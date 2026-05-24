package org.gabbard.pandemicgenerator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.Random

/**
 * Verifies that TrackableState remains fully serializable after every transition.
 *
 * The Android UI passes game state between activities via Intent extras using Java
 * serialization. Any non-Serializable field in the state (or its event log) will
 * throw NotSerializableException at runtime, causing the current activity to crash
 * and dumping the user back to the previous screen with no explanation.
 */
class SerializationTest {

    private val rng = Random(42)

    private fun makeState(
        playerCards: List<PlayerCard> = ALL_CITIES.take(5).map { CityPlayerCard(it) },
        lastTransition: Transition = Transition.INFECT
    ): TrackableState {
        val players = listOf(Player(Role("Medic")), Player(Role("Scientist")))
        return TrackableState(
            curPlayer = 0,
            players = players,
            infectionDeck = Deck(ALL_CITIES.take(10).map { InfectionCard(it) }),
            infectionDiscardPile = ALL_CITIES.drop(10).map { InfectionCard(it) }.toSet(),
            playerDeck = Deck(playerCards),
            infectionRate = InfectionRate.INITIAL,
            lastTransition = lastTransition
        )
    }

    private fun roundTrip(state: TrackableState): TrackableState {
        val baos = ByteArrayOutputStream()
        ObjectOutputStream(baos).use { it.writeObject(state) }
        val bytes = baos.toByteArray()
        return ObjectInputStream(ByteArrayInputStream(bytes)).use {
            it.readObject() as TrackableState
        }
    }

    @Test
    fun initialStateIsSerializable() {
        val state = makeState()
        val restored = roundTrip(state)
        assertEquals(state, restored)
    }

    @Test
    fun stateAfterDrawPlayerCardsIsSerializable() {
        val state = makeState()
        val result = state.executeTransition(Transition.DRAW_PLAYER_CARDS, rng)
                as TrackableState.TransitionResult.Success.DrawPlayerCardsTransitionResult
        val restored = roundTrip(result.newGameState)
        assertEquals(result.newGameState, restored)
    }

    @Test
    fun stateAfterDrawWithEpidemicIsSerializable() {
        val epidemic = NamedEpidemic("Test Epidemic")
        val cards = listOf(epidemic) + ALL_CITIES.take(4).map { CityPlayerCard(it) }
        val state = makeState(playerCards = cards)
        val result = state.executeTransition(Transition.DRAW_PLAYER_CARDS, rng)
                as TrackableState.TransitionResult.Success.DrawPlayerCardsTransitionResult
        val restored = roundTrip(result.newGameState)
        assertEquals(result.newGameState, restored)
    }

    @Test
    fun stateAfterInfectIsSerializable() {
        val state = makeState(lastTransition = Transition.DRAW_PLAYER_CARDS)
        val result = state.executeTransition(Transition.INFECT, rng)
                as TrackableState.TransitionResult.Success.InfectionTransitionResult
        val restored = roundTrip(result.newGameState)
        assertEquals(result.newGameState, restored)
    }

    @Test
    fun stateAfterFullTurnCycleIsSerializable() {
        // 3 turns × 2 cards drawn = 6 cards needed; give 7 so the deck is never exhausted.
        val state = makeState(playerCards = ALL_CITIES.take(7).map { CityPlayerCard(it) })
        var current = state
        repeat(3) {
            val drawResult = current.executeTransition(Transition.DRAW_PLAYER_CARDS, rng)
                    as TrackableState.TransitionResult.Success.DrawPlayerCardsTransitionResult
            current = roundTrip(drawResult.newGameState)

            val infectResult = current.executeTransition(Transition.INFECT, rng)
                    as TrackableState.TransitionResult.Success.InfectionTransitionResult
            current = roundTrip(infectResult.newGameState)
        }
        assertEquals(6, current.eventLog.size)
    }

    @Test
    fun eventLogCardsDrawnIsNotASubListView() {
        // Regression test: Deck.draw() previously returned cards.subList(0, n), an
        // ArrayList$SubList that is not Serializable. The drawn cards end up stored
        // in DrawPlayerCardsEvent.cardsDrawn inside gameState.eventLog, so any
        // non-serializable view causes NotSerializableException when the Android UI
        // passes the state between activities via Intent extras.
        val state = makeState()
        val result = state.executeTransition(Transition.DRAW_PLAYER_CARDS, rng)
                as TrackableState.TransitionResult.Success.DrawPlayerCardsTransitionResult
        val event = result.newGameState.eventLog.last() as GameEvent.DrawPlayerCardsEvent
        // If cardsDrawn is a subList view this will throw NotSerializableException
        val baos = ByteArrayOutputStream()
        ObjectOutputStream(baos).use { it.writeObject(event.cardsDrawn) }
        assertTrue("cardsDrawn must be serializable", baos.size() > 0)
    }
}
