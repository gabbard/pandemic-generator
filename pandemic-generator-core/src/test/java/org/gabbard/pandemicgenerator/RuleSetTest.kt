package org.gabbard.pandemicgenerator

import org.junit.Assert.*
import org.junit.Test
import java.util.Random

class RuleSetTest {

    private val rng = Random(42)

    // ── setupGame structural invariants ──────────────────────────────────────

    @Test
    fun setupGameProducesCorrectNumberOfPlayers() {
        val game = NATIONAL_CHAMPIONSHIP_RULES.setupGame(rng)
        assertEquals(2, game.trackableState.players.size)
    }

    @Test
    fun allPlayersHaveDistinctRoles() {
        val game = NATIONAL_CHAMPIONSHIP_RULES.setupGame(rng)
        val roles = game.trackableState.players.map { it.role }
        assertEquals(roles.size, roles.toSet().size)
    }

    @Test
    fun openingHandsAreFourCards() {
        val game = NATIONAL_CHAMPIONSHIP_RULES.setupGame(rng)
        for ((_, hand) in game.untrackableState.hands) {
            assertEquals(4, hand.size)
        }
    }

    @Test
    fun infectionDeckContainsAllCities() {
        val game = NATIONAL_CHAMPIONSHIP_RULES.setupGame(rng)
        val allCitiesInDeckOrDiscard =
            game.trackableState.infectionDeck.cards.map { it.city }.toSet() +
            game.trackableState.infectionDiscardPile.map { it.city }.toSet()
        assertEquals(ALL_CITIES, allCitiesInDeckOrDiscard)
    }

    @Test
    fun nineInitialInfectionCardsAreInDiscard() {
        val game = NATIONAL_CHAMPIONSHIP_RULES.setupGame(rng)
        assertEquals(9, game.trackableState.infectionDiscardPile.size)
        assertEquals(ALL_CITIES.size - 9, game.trackableState.infectionDeck.cards.size)
    }

    @Test
    fun initialBoardHasCorrectCubeDistribution() {
        val game = NATIONAL_CHAMPIONSHIP_RULES.setupGame(rng)
        val board = game.untrackableState.board
        val cubeCounts = board.cityStates.values.map { it.infections.values.sum() }
        assertEquals(3, cubeCounts.count { it == 3 })
        assertEquals(3, cubeCounts.count { it == 2 })
        assertEquals(3, cubeCounts.count { it == 1 })
    }

    @Test
    fun playerDeckContainsCorrectNumberOfEpidemics() {
        val game = NATIONAL_CHAMPIONSHIP_RULES.setupGame(rng)
        val epidemicsInDeck = game.trackableState.playerDeck.cards.filterIsInstance<Epidemic>()
        assertEquals(NATIONAL_CHAMPIONSHIP_RULES.numEpidemicsToUse, epidemicsInDeck.size)
    }

    @Test
    fun initialInfectionRateIsInitial() {
        val game = NATIONAL_CHAMPIONSHIP_RULES.setupGame(rng)
        assertEquals(InfectionRate.INITIAL, game.trackableState.infectionRate)
    }

    @Test
    fun initialLastTransitionIsInfect() {
        // game starts ready for the player to draw cards
        val game = NATIONAL_CHAMPIONSHIP_RULES.setupGame(rng)
        assertEquals(Transition.INFECT, game.trackableState.lastTransition)
    }

    @Test
    fun setupIsDeterministicForSameSeed() {
        val game1 = NATIONAL_CHAMPIONSHIP_RULES.setupGame(Random(99))
        val game2 = NATIONAL_CHAMPIONSHIP_RULES.setupGame(Random(99))
        assertEquals(
            game1.trackableState.players.map { it.role },
            game2.trackableState.players.map { it.role }
        )
        assertEquals(
            game1.trackableState.infectionDeck.cards,
            game2.trackableState.infectionDeck.cards
        )
    }

    @Test
    fun setupProducesDifferentGamesForDifferentSeeds() {
        val game1 = NATIONAL_CHAMPIONSHIP_RULES.setupGame(Random(1))
        val game2 = NATIONAL_CHAMPIONSHIP_RULES.setupGame(Random(2))
        // Vanishingly unlikely to be identical
        assertNotEquals(
            game1.trackableState.infectionDeck.cards,
            game2.trackableState.infectionDeck.cards
        )
    }

    // ── buildGameRules validation ─────────────────────────────────────────────

    @Test(expected = IllegalArgumentException::class)
    fun buildGameRulesRejectsPlayerCountNotInAllowedList() {
        NATIONAL_CHAMPIONSHIP.buildGameRules(
            GameOptions(numPlayers = 3, difficulty = NATIONAL_CHAMPIONSHIP_DIFFICULTY)
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun buildGameRulesRejectsDifficultyNotInAvailableList() {
        NATIONAL_CHAMPIONSHIP.buildGameRules(
            GameOptions(numPlayers = 2, difficulty = Difficulty("Unknown", 4))
        )
    }

    // ── NATIONAL_CHAMPIONSHIP configuration ───────────────────────────────────

    @Test
    fun nationalChampionshipAllowsOnlyTwoPlayers() {
        assertEquals(listOf(2), NATIONAL_CHAMPIONSHIP.allowedPlayerCounts)
    }

    @Test
    fun nationalChampionshipTurnDurationIs75Seconds() {
        assertEquals(75, NATIONAL_CHAMPIONSHIP.turnDurationSeconds)
    }

    @Test
    fun nationalChampionshipRulesPropagateTurnDuration() {
        assertEquals(75, NATIONAL_CHAMPIONSHIP_RULES.turnDurationSeconds)
    }

    @Test
    fun nationalChampionshipHasExactlySixAvailableEpidemics() {
        assertEquals(6, NATIONAL_CHAMPIONSHIP.availableEpidemics.size)
    }

    // ── STANDARD_PANDEMIC configuration ──────────────────────────────────────

    @Test
    fun standardPandemicHasNoTimer() {
        assertNull(STANDARD_PANDEMIC.turnDurationSeconds)
    }

    @Test
    fun standardPandemicAllows2To4Players() {
        assertEquals(listOf(2, 3, 4), STANDARD_PANDEMIC.allowedPlayerCounts)
    }

    @Test
    fun standardPandemicIntroductoryHasFourEpidemics() {
        val rules = STANDARD_PANDEMIC.buildGameRules(GameOptions(2, Difficulty("Introductory", 4)))
        assertEquals(4, rules.numEpidemicsToUse)
    }

    @Test
    fun standardPandemicNormalHasFiveEpidemics() {
        val rules = STANDARD_PANDEMIC.buildGameRules(GameOptions(2, Difficulty("Normal", 5)))
        assertEquals(5, rules.numEpidemicsToUse)
    }

    @Test
    fun standardPandemicHeroicHasSixEpidemics() {
        val rules = STANDARD_PANDEMIC.buildGameRules(GameOptions(2, Difficulty("Heroic", 6)))
        assertEquals(6, rules.numEpidemicsToUse)
    }

    // ── GENCON_2026 configuration ─────────────────────────────────────────────

    @Test
    fun genCon2026Allows2To4Players() {
        assertEquals(listOf(2, 3, 4), GENCON_2026.allowedPlayerCounts)
    }

    @Test
    fun genCon2026TurnDurationIs75Seconds() {
        assertEquals(75, GENCON_2026.turnDurationSeconds)
    }

    @Test
    fun genCon2026UsesSimpleEpidemics() {
        assertTrue(GENCON_2026.availableEpidemics.all { it is SimpleEpidemic })
    }

    @Test
    fun genCon2026IntroductoryHasFourEpidemics() {
        val rules = GENCON_2026.buildGameRules(GameOptions(2, Difficulty("Introductory", 4)))
        assertEquals(4, rules.numEpidemicsToUse)
    }

    @Test
    fun genCon2026NormalHasFiveEpidemics() {
        val rules = GENCON_2026.buildGameRules(GameOptions(2, Difficulty("Normal", 5)))
        assertEquals(5, rules.numEpidemicsToUse)
    }

    @Test
    fun genCon2026HeroicHasSixEpidemics() {
        val rules = GENCON_2026.buildGameRules(GameOptions(2, Difficulty("Heroic", 6)))
        assertEquals(6, rules.numEpidemicsToUse)
    }

    @Test
    fun genCon2026UsesAllCurrentlyAvailableRoles() {
        assertEquals(ALL_ROLES - INFECTION_DECK_INTERACTION_ROLES, GENCON_2026.availableRoles)
    }

    @Test
    fun genCon2026UsesAllCurrentlyAvailableEvents() {
        assertEquals(ALL_KNOWN_EVENTS, GENCON_2026.availableEvents)
        assertEquals(ALL_KNOWN_EVENTS.size, GENCON_2026.numEventsToUse)
    }

    // ── BUILT_IN_RULE_SETS ────────────────────────────────────────────────────

    @Test
    fun builtInRuleSetsContainsAllVariants() {
        assertTrue(BUILT_IN_RULE_SETS.contains(STANDARD_PANDEMIC))
        assertTrue(BUILT_IN_RULE_SETS.contains(NATIONAL_CHAMPIONSHIP))
        assertTrue(BUILT_IN_RULE_SETS.contains(GENCON_2026))
    }

    // ── turn order across a full game (regression test for #55) ─────────────

    @Test
    fun curPlayerAlternatesCorrectlyAcrossTurnsIncludingAnEpidemic() {
        val rng = Random(713)
        var state = NATIONAL_CHAMPIONSHIP_RULES.setupGame(rng).trackableState
        var sawEpidemic = false

        repeat(6) { turn ->
            assertEquals("wrong current player before turn $turn", turn % 2, state.curPlayer)

            val drawResult = state.executeTransition(Transition.DRAW_PLAYER_CARDS, rng)
                    as TrackableState.TransitionResult.Success.DrawPlayerCardsTransitionResult
            if (drawResult.epidemicsAndInfectedCities.isNotEmpty()) {
                sawEpidemic = true
            }
            state = drawResult.newGameState
            // still the same player's turn until they finish infecting
            assertEquals("curPlayer changed during draw on turn $turn", turn % 2, state.curPlayer)

            state = (state.executeTransition(Transition.INFECT, rng)
                    as TrackableState.TransitionResult.Success).newGameState
            assertEquals("curPlayer did not advance after turn $turn", (turn + 1) % 2, state.curPlayer)
        }

        assertTrue("test seed should have produced an epidemic to exercise the regression", sawEpidemic)
    }

    // ── chooseDistinct ────────────────────────────────────────────────────────

    @Test
    fun chooseDistinctReturnsRequestedCount() {
        val items = (1..10).toSet()
        val chosen = chooseDistinct(items, 4, rng)
        assertEquals(4, chosen.size)
    }

    @Test
    fun chooseDistinctReturnsSubset() {
        val items = (1..10).toSet()
        val chosen = chooseDistinct(items, 4, rng)
        assertTrue(items.containsAll(chosen))
    }

    @Test(expected = IllegalArgumentException::class)
    fun chooseDistinctThrowsWhenNotEnoughItems() {
        chooseDistinct(setOf(1, 2), 5, rng)
    }
}
