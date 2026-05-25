package org.gabbard.pandemicgenerator

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class GameStateTest {

    private val cities = ALL_CITIES.toList()

    private fun makeTrackableState(
        players: List<Player> = listOf(Player(Role("Medic")), Player(Role("Scientist")))
    ) = TrackableState(
        curPlayer = 0,
        players = players,
        infectionDeck = Deck(cities.take(10).map { InfectionCard(it) }),
        infectionDiscardPile = cities.drop(10).map { InfectionCard(it) }.toSet(),
        playerDeck = Deck(cities.take(5).map { CityPlayerCard(it) }),
        infectionRate = InfectionRate.INITIAL,
        lastTransition = Transition.INFECT
    )

    private fun makeUntrackableState(
        players: List<Player>,
        handSize: Int = initialHandSize(players.size)
    ): UntrackableState {
        val hands = players.associateWith { player ->
            cities.takeLast(handSize).map { CityPlayerCard(it) }.toSet<PlayerCard>()
        }
        return UntrackableState(
            board = BoardState(emptyMap()),
            hands = hands
        )
    }

    // ── valid construction ────────────────────────────────────────────────────

    @Test
    fun validGameStateConstructsSuccessfully() {
        val players = listOf(Player(Role("Medic")), Player(Role("Scientist")))
        val trackable = makeTrackableState(players)
        val untrackable = makeUntrackableState(players)
        val gameState = GameState(trackable, untrackable)
        assertNotNull(gameState)
    }

    @Test
    fun gameStateHasCorrectTrackableState() {
        val players = listOf(Player(Role("Medic")), Player(Role("Scientist")))
        val trackable = makeTrackableState(players)
        val untrackable = makeUntrackableState(players)
        val gameState = GameState(trackable, untrackable)
        assertEquals(trackable, gameState.trackableState)
    }

    @Test
    fun gameStateHasCorrectUntrackableState() {
        val players = listOf(Player(Role("Medic")), Player(Role("Scientist")))
        val trackable = makeTrackableState(players)
        val untrackable = makeUntrackableState(players)
        val gameState = GameState(trackable, untrackable)
        assertEquals(untrackable, gameState.untrackableState)
    }

    // ── player/hand mismatch validation ──────────────────────────────────────

    @Test(expected = IllegalArgumentException::class)
    fun mismatcedPlayersAndHandsThrows() {
        val players = listOf(Player(Role("Medic")), Player(Role("Scientist")))
        val otherPlayer = Player(Role("Dispatcher"))
        val trackable = makeTrackableState(players)
        // Hands keyed by a different player set
        val untrackable = UntrackableState(
            board = BoardState(emptyMap()),
            hands = mapOf(players[0] to emptySet(), otherPlayer to emptySet())
        )
        GameState(trackable, untrackable) // must throw
    }

    @Test(expected = IllegalArgumentException::class)
    fun wrongHandSizeThrows() {
        val players = listOf(Player(Role("Medic")), Player(Role("Scientist")))
        val trackable = makeTrackableState(players)
        // initialHandSize(2) = 4; give 1 card instead
        val untrackable = UntrackableState(
            board = BoardState(emptyMap()),
            hands = players.associateWith { setOf<PlayerCard>(CityPlayerCard(cities[0])) }
        )
        GameState(trackable, untrackable) // must throw due to wrong hand size
    }

    // ── setupGame round-trip ──────────────────────────────────────────────────

    @Test
    fun setupGameProducesValidGameState() {
        val gameState = NATIONAL_CHAMPIONSHIP_RULES.setupGame(java.util.Random(42))
        assertNotNull(gameState)
        assertEquals(2, gameState.trackableState.players.size)
    }

    @Test
    fun setupGameHandSizeMatchesPlayerCount() {
        val gameState = NATIONAL_CHAMPIONSHIP_RULES.setupGame(java.util.Random(42))
        val expectedHandSize = initialHandSize(2)
        for ((_, hand) in gameState.untrackableState.hands) {
            assertEquals(expectedHandSize, hand.size)
        }
    }
}
