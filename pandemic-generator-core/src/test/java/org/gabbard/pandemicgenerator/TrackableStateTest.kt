package org.gabbard.pandemicgenerator

import org.junit.Assert.*
import org.junit.Test
import java.util.Random

class TrackableStateTest {

    private val rng = Random(0)

    private fun makeState(
        infectionDeckCities: List<City> = ALL_CITIES.take(10).toList(),
        discardCities: Set<City> = ALL_CITIES.drop(10).toSet(),
        playerCards: List<PlayerCard> = listOf(SimpleEpidemic(), SimpleEpidemic()),
        infectionRate: InfectionRate = InfectionRate.INITIAL,
        lastTransition: Transition = Transition.DRAW_PLAYER_CARDS
    ): TrackableState {
        val players = listOf(Player(Role("Medic")), Player(Role("Scientist")))
        return TrackableState(
            curPlayer = 0,
            players = players,
            infectionDeck = Deck(infectionDeckCities.map { InfectionCard(it) }),
            infectionDiscardPile = discardCities.map { InfectionCard(it) }.toSet(),
            playerDeck = Deck(playerCards),
            infectionRate = infectionRate,
            lastTransition = lastTransition
        )
    }

    // ── legalTransitions ────────────────────────────────────────────────────

    @Test
    fun afterInfectOnlyDrawIsLegal() {
        val state = makeState(lastTransition = Transition.INFECT)
        assertEquals(setOf(Transition.DRAW_PLAYER_CARDS), state.legalTransitions())
    }

    @Test
    fun afterDrawOnlyInfectIsLegal() {
        val state = makeState(lastTransition = Transition.DRAW_PLAYER_CARDS)
        assertEquals(setOf(Transition.INFECT), state.legalTransitions())
    }

    // ── infect transition ────────────────────────────────────────────────────

    @Test
    fun infectDrawsCorrectNumberOfCards() {
        val state = makeState(lastTransition = Transition.DRAW_PLAYER_CARDS,
            infectionRate = InfectionRate.INITIAL)
        val result = state.executeTransition(Transition.INFECT, rng)
                as TrackableState.TransitionResult.Success.InfectionTransitionResult
        assertEquals(InfectionRate.INITIAL.numInfectionCardsToDraw, result.infectedCities.size)
    }

    @Test
    fun infectMovesCardsToDiscard() {
        val infectionCities = ALL_CITIES.take(10).toList()
        val discardCities = ALL_CITIES.drop(10).toSet()
        val state = makeState(
            infectionDeckCities = infectionCities,
            discardCities = discardCities,
            lastTransition = Transition.DRAW_PLAYER_CARDS
        )
        val result = state.executeTransition(Transition.INFECT, rng)
                as TrackableState.TransitionResult.Success.InfectionTransitionResult
        val newState = result.newGameState

        // drawn cities moved from deck to discard
        val numDrawn = InfectionRate.INITIAL.numInfectionCardsToDraw
        assertEquals(infectionCities.size - numDrawn, newState.infectionDeck.cards.size)
        assertTrue(newState.infectionDiscardPile.map { it.city }.containsAll(result.infectedCities))
    }

    @Test
    fun infectCitiesAreTopOfDeck() {
        val cities = ALL_CITIES.take(10).toList()
        val state = makeState(
            infectionDeckCities = cities,
            discardCities = ALL_CITIES.drop(10).toSet(),
            lastTransition = Transition.DRAW_PLAYER_CARDS
        )
        val result = state.executeTransition(Transition.INFECT, rng)
                as TrackableState.TransitionResult.Success.InfectionTransitionResult
        val numDrawn = InfectionRate.INITIAL.numInfectionCardsToDraw
        assertEquals(cities.take(numDrawn), result.infectedCities)
    }

    @Test
    fun infectSetsLastTransition() {
        val state = makeState(lastTransition = Transition.DRAW_PLAYER_CARDS)
        val result = state.executeTransition(Transition.INFECT, rng)
                as TrackableState.TransitionResult.Success.InfectionTransitionResult
        assertEquals(Transition.INFECT, result.newGameState.lastTransition)
    }

    // ── draw player cards transition ─────────────────────────────────────────

    @Test
    fun drawPlayerCardsDrawsTwo() {
        val cityCards = ALL_CITIES.take(5).map { CityPlayerCard(it) }
        val state = makeState(playerCards = cityCards, lastTransition = Transition.INFECT)
        val result = state.executeTransition(Transition.DRAW_PLAYER_CARDS, rng)
                as TrackableState.TransitionResult.Success.DrawPlayerCardsTransitionResult
        assertEquals(2, result.cardsDrawn.size)
        assertEquals(3, result.newGameState.playerDeck.cards.size)
    }

    @Test
    fun drawPlayerCardsSetsLastTransition() {
        val cityCards = ALL_CITIES.take(5).map { CityPlayerCard(it) }
        val state = makeState(playerCards = cityCards, lastTransition = Transition.INFECT)
        val result = state.executeTransition(Transition.DRAW_PLAYER_CARDS, rng)
                as TrackableState.TransitionResult.Success.DrawPlayerCardsTransitionResult
        assertEquals(Transition.DRAW_PLAYER_CARDS, result.newGameState.lastTransition)
    }

    @Test
    fun drawWithNoEpidemicsHasEmptyEpidemicList() {
        val cityCards = ALL_CITIES.take(5).map { CityPlayerCard(it) }
        val state = makeState(playerCards = cityCards, lastTransition = Transition.INFECT)
        val result = state.executeTransition(Transition.DRAW_PLAYER_CARDS, rng)
                as TrackableState.TransitionResult.Success.DrawPlayerCardsTransitionResult
        assertTrue(result.epidemicsAndInfectedCities.isEmpty())
    }

    // ── epidemic execution ────────────────────────────────────────────────────

    @Test
    fun epidemicAdvancesInfectionRate() {
        val epidemic = NamedEpidemic("Test Epidemic")
        val cityCards = listOf(epidemic) + ALL_CITIES.take(4).map { CityPlayerCard(it) }
        val state = makeState(
            playerCards = cityCards,
            infectionRate = InfectionRate.INITIAL,
            lastTransition = Transition.INFECT
        )
        val result = state.executeTransition(Transition.DRAW_PLAYER_CARDS, rng)
                as TrackableState.TransitionResult.Success.DrawPlayerCardsTransitionResult
        assertEquals(InfectionRate.STEP_TWO, result.newGameState.infectionRate)
    }

    @Test
    fun epidemicClearsDiscardPile() {
        val epidemic = NamedEpidemic("Test Epidemic")
        val cityCards = listOf(epidemic) + ALL_CITIES.take(4).map { CityPlayerCard(it) }
        val state = makeState(
            playerCards = cityCards,
            lastTransition = Transition.INFECT
        )
        val result = state.executeTransition(Transition.DRAW_PLAYER_CARDS, rng)
                as TrackableState.TransitionResult.Success.DrawPlayerCardsTransitionResult
        assertTrue(result.newGameState.infectionDiscardPile.isEmpty())
    }

    @Test
    fun epidemicInfectsCityFromBottomOfDeck() {
        val epidemic = NamedEpidemic("Test Epidemic")
        val cityCards = listOf(epidemic) + ALL_CITIES.take(4).map { CityPlayerCard(it) }
        val infectionCities = ALL_CITIES.take(10).toList()
        val state = makeState(
            playerCards = cityCards,
            infectionDeckCities = infectionCities,
            discardCities = ALL_CITIES.drop(10).toSet(),
            lastTransition = Transition.INFECT
        )
        val result = state.executeTransition(Transition.DRAW_PLAYER_CARDS, rng)
                as TrackableState.TransitionResult.Success.DrawPlayerCardsTransitionResult
        // bottom of deck = last element
        val expectedCity = infectionCities.last()
        assertEquals(1, result.epidemicsAndInfectedCities.size)
        assertEquals(expectedCity, result.epidemicsAndInfectedCities[0].second)
    }

    // ── event log ────────────────────────────────────────────────────────────

    @Test
    fun infectAppendsInfectionEventToLog() {
        val state = makeState(lastTransition = Transition.DRAW_PLAYER_CARDS)
        val result = state.executeTransition(Transition.INFECT, rng)
                as TrackableState.TransitionResult.Success.InfectionTransitionResult
        val log = result.newGameState.eventLog
        assertEquals(1, log.size)
        val event = log[0] as GameEvent.InfectionEvent
        assertEquals(result.infectedCities, event.infectedCities)
    }

    @Test
    fun drawPlayerCardsAppendsDrawEventToLog() {
        val cityCards = ALL_CITIES.take(5).map { CityPlayerCard(it) }
        val state = makeState(playerCards = cityCards, lastTransition = Transition.INFECT)
        val result = state.executeTransition(Transition.DRAW_PLAYER_CARDS, rng)
                as TrackableState.TransitionResult.Success.DrawPlayerCardsTransitionResult
        val log = result.newGameState.eventLog
        assertEquals(1, log.size)
        val event = log[0] as GameEvent.DrawPlayerCardsEvent
        assertEquals(result.cardsDrawn, event.cardsDrawn)
        assertEquals(result.epidemicsAndInfectedCities, event.epidemicsAndInfectedCities)
    }

    @Test
    fun epidemicEventRecordsEpidemicCityPair() {
        val epidemic = NamedEpidemic("Test Epidemic")
        val cityCards = listOf(epidemic) + ALL_CITIES.take(4).map { CityPlayerCard(it) }
        val state = makeState(playerCards = cityCards, lastTransition = Transition.INFECT)
        val result = state.executeTransition(Transition.DRAW_PLAYER_CARDS, rng)
                as TrackableState.TransitionResult.Success.DrawPlayerCardsTransitionResult
        val event = result.newGameState.eventLog[0] as GameEvent.DrawPlayerCardsEvent
        assertEquals(1, event.epidemicsAndInfectedCities.size)
        assertEquals(result.epidemicsAndInfectedCities, event.epidemicsAndInfectedCities)
    }

    @Test
    fun eventLogAccumulatesAcrossTransitions() {
        val cityCards = ALL_CITIES.take(5).map { CityPlayerCard(it) }
        val state = makeState(playerCards = cityCards, lastTransition = Transition.INFECT)
        val afterDraw = (state.executeTransition(Transition.DRAW_PLAYER_CARDS, rng)
                as TrackableState.TransitionResult.Success).newGameState
        val afterInfect = (afterDraw.executeTransition(Transition.INFECT, rng)
                as TrackableState.TransitionResult.Success).newGameState
        assertEquals(2, afterInfect.eventLog.size)
        assertTrue(afterInfect.eventLog[0] is GameEvent.DrawPlayerCardsEvent)
        assertTrue(afterInfect.eventLog[1] is GameEvent.InfectionEvent)
    }

    @Test
    fun initialStateHasEmptyEventLog() {
        val state = makeState()
        assertTrue(state.eventLog.isEmpty())
    }

    // ── player deck exhaustion ───────────────────────────────────────────────

    @Test
    fun drawPlayerCardsReturnsExhaustedWhenDeckIsEmpty() {
        val state = makeState(playerCards = emptyList(), lastTransition = Transition.INFECT)
        val result = state.executeTransition(Transition.DRAW_PLAYER_CARDS, rng)
        assertEquals(TrackableState.TransitionResult.PlayerDeckExhausted, result)
    }

    @Test
    fun drawPlayerCardsReturnsExhaustedWhenOnlyOneCardRemains() {
        val state = makeState(
            playerCards = listOf(CityPlayerCard(ALL_CITIES.first())),
            lastTransition = Transition.INFECT
        )
        val result = state.executeTransition(Transition.DRAW_PLAYER_CARDS, rng)
        assertEquals(TrackableState.TransitionResult.PlayerDeckExhausted, result)
    }

    @Test
    fun drawPlayerCardsSucceedsWhenExactlyTwoCardsRemain() {
        val cityCards = ALL_CITIES.take(2).map { CityPlayerCard(it) }
        val state = makeState(playerCards = cityCards, lastTransition = Transition.INFECT)
        val result = state.executeTransition(Transition.DRAW_PLAYER_CARDS, rng)
        assertTrue(result is TrackableState.TransitionResult.Success.DrawPlayerCardsTransitionResult)
    }

    @Test
    fun epidemicShufflesDiscardOntoTopOfDeck() {
        val epidemic = NamedEpidemic("Test Epidemic")
        val cityCards = listOf(epidemic) + ALL_CITIES.take(4).map { CityPlayerCard(it) }
        val infectionCities = ALL_CITIES.take(10).toList()
        val discardCities = ALL_CITIES.drop(10).toSet()
        val state = makeState(
            playerCards = cityCards,
            infectionDeckCities = infectionCities,
            discardCities = discardCities,
            lastTransition = Transition.INFECT
        )
        val preDeckSize = infectionCities.size
        val preDiscardSize = discardCities.size

        val result = state.executeTransition(Transition.DRAW_PLAYER_CARDS, rng)
                as TrackableState.TransitionResult.Success.DrawPlayerCardsTransitionResult
        val newDeck = result.newGameState.infectionDeck

        // new deck = old discard (shuffled) + old bottom-card + remaining deck cards
        // size = preDiscard + 1 + (preDeck - 1) = preDiscard + preDeck
        assertEquals(preDiscardSize + preDeckSize, newDeck.cards.size)
    }
}
