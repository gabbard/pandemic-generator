package org.gabbard.pandemicgenerator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Random

class SetupGameTest {

    private val rng = Random(42)

    // ── initialHandSize ───────────────────────────────────────────────────────

    @Test fun initialHandSizeForTwoPlayers()   = assertEquals(4, initialHandSize(2))
    @Test fun initialHandSizeForThreePlayers() = assertEquals(3, initialHandSize(3))
    @Test fun initialHandSizeForFourPlayers()  = assertEquals(2, initialHandSize(4))

    // ── infection deck setup ──────────────────────────────────────────────────

    @Test
    fun infectionDeckHas39CardsAfterSetup() {
        val state = NATIONAL_CHAMPIONSHIP_RULES.setupGame(rng).trackableState
        assertEquals(39, state.infectionDeck.cards.size)
    }

    @Test
    fun initialInfectionDiscardHasNineCards() {
        val state = NATIONAL_CHAMPIONSHIP_RULES.setupGame(rng).trackableState
        assertEquals(9, state.infectionDiscardPile.size)
    }

    @Test
    fun infectionDeckAndDiscardCoverAllCities() {
        val state = NATIONAL_CHAMPIONSHIP_RULES.setupGame(rng).trackableState
        val allInfectionCities = (state.infectionDeck.cards + state.infectionDiscardPile)
            .map { it.city }.toSet()
        assertEquals(ALL_CITIES, allInfectionCities)
    }

    // ── player hand sizes ─────────────────────────────────────────────────────

    @Test
    fun twoPlayerHandsHaveFourCardsEach() {
        val hands = NATIONAL_CHAMPIONSHIP_RULES.setupGame(rng).untrackableState.hands
        assertEquals(2, hands.size)
        hands.values.forEach { hand -> assertEquals(4, hand.size) }
    }

    @Test
    fun threePlayerHandsHaveThreeCardsEach() {
        val rules = STANDARD_PANDEMIC.buildGameRules(GameOptions(3, Difficulty("Normal", 5)))
        val hands = rules.setupGame(rng).untrackableState.hands
        assertEquals(3, hands.size)
        hands.values.forEach { hand -> assertEquals(3, hand.size) }
    }

    @Test
    fun fourPlayerHandsHaveTwoCardsEach() {
        val rules = STANDARD_PANDEMIC.buildGameRules(GameOptions(4, Difficulty("Heroic", 6)))
        val hands = rules.setupGame(rng).untrackableState.hands
        assertEquals(4, hands.size)
        hands.values.forEach { hand -> assertEquals(2, hand.size) }
    }

    @Test
    fun playerHandsContainNoEpidemics() {
        val hands = NATIONAL_CHAMPIONSHIP_RULES.setupGame(rng).untrackableState.hands
        val anyEpidemicInHands = hands.values.flatten().any { it is Epidemic }
        assertFalse("Player hands must not contain epidemic cards at game start", anyEpidemicInHands)
    }

    // ── player deck composition ───────────────────────────────────────────────

    @Test
    fun playerDeckContainsCorrectNumberOfEpidemics() {
        val state = NATIONAL_CHAMPIONSHIP_RULES.setupGame(rng).trackableState
        val epidemicsInDeck = state.playerDeck.cards.filterIsInstance<Epidemic>()
        assertEquals(6, epidemicsInDeck.size)
    }

    @Test
    fun playerDeckSizeAccountsForDealtHandsAndEpidemics() {
        // NATIONAL_CHAMPIONSHIP: 48 cities + 5 events = 53 cards.
        // Deal 2 hands of 4 = 8. Remaining = 45. Add 6 epidemics = 51.
        val state = NATIONAL_CHAMPIONSHIP_RULES.setupGame(rng).trackableState
        assertEquals(51, state.playerDeck.cards.size)
    }

    // ── initial game state ────────────────────────────────────────────────────

    @Test
    fun initialCurrentPlayerIsZero() {
        val state = NATIONAL_CHAMPIONSHIP_RULES.setupGame(rng).trackableState
        assertEquals(0, state.curPlayer)
    }

    @Test
    fun initialLastTransitionIsInfect() {
        val state = NATIONAL_CHAMPIONSHIP_RULES.setupGame(rng).trackableState
        assertEquals(Transition.INFECT, state.lastTransition)
    }

    @Test
    fun initialInfectionRateIsInitial() {
        val state = NATIONAL_CHAMPIONSHIP_RULES.setupGame(rng).trackableState
        assertEquals(InfectionRate.INITIAL, state.infectionRate)
    }

    @Test
    fun setupGameCreatesCorrectNumberOfPlayers() {
        val state = NATIONAL_CHAMPIONSHIP_RULES.setupGame(rng).trackableState
        assertEquals(2, state.players.size)
    }

    @Test
    fun setupGamePlayersHaveDistinctRoles() {
        val state = NATIONAL_CHAMPIONSHIP_RULES.setupGame(rng).trackableState
        val roles = state.players.map { it.role }.toSet()
        assertEquals(state.players.size, roles.size)
    }

    @Test
    fun setupGameRolesAreDrawnFromAvailablePool() {
        val state = NATIONAL_CHAMPIONSHIP_RULES.setupGame(rng).trackableState
        val assignedRoles = state.players.map { it.role }.toSet()
        assertTrue(NATIONAL_CHAMPIONSHIP_RULES.availableRoles.containsAll(assignedRoles))
    }
}
