package gabbard.org.pandemicgenerator

import android.content.Intent
import android.widget.LinearLayout
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import org.gabbard.pandemicgenerator.ALL_CITIES
import org.gabbard.pandemicgenerator.BoardState
import org.gabbard.pandemicgenerator.CityPlayerCard
import org.gabbard.pandemicgenerator.CityState
import org.gabbard.pandemicgenerator.Color
import org.gabbard.pandemicgenerator.Deck
import org.gabbard.pandemicgenerator.Epidemic
import org.gabbard.pandemicgenerator.GameEvent
import org.gabbard.pandemicgenerator.InfectionCard
import org.gabbard.pandemicgenerator.InfectionRate
import org.gabbard.pandemicgenerator.NamedEpidemic
import org.gabbard.pandemicgenerator.Player
import org.gabbard.pandemicgenerator.PlayerCard
import org.gabbard.pandemicgenerator.Role
import org.gabbard.pandemicgenerator.TrackableState
import org.gabbard.pandemicgenerator.Transition
import org.gabbard.pandemicgenerator.UntrackableState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Random

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class GameLogActivityTest {

    private val cities = ALL_CITIES.toList()
    private val rng = Random(42)

    // ── empty log ─────────────────────────────────────────────────────────────

    @Test
    fun emptyLogShowsNoEventsHeader() {
        val state = makeState()
        ActivityScenario.launch<GameLogActivity>(intentFor(state)).use { scenario ->
            scenario.onActivity { activity ->
                val container = activity.findViewById<LinearLayout>(R.id.eventLogContainer)
                // addSectionHeader adds one TextView child
                assertEquals(1, container.childCount)
                val header = container.getChildAt(0) as TextView
                assertTrue(header.text.contains("No events yet"))
            }
        }
    }

    @Test
    fun seedIsDisplayedInFooter() {
        val state = makeState()
        ActivityScenario.launch<GameLogActivity>(intentFor(state, seed = 99L)).use { scenario ->
            scenario.onActivity { activity ->
                val seedView = activity.findViewById<TextView>(R.id.seedDisplay)
                assertTrue(seedView.text.contains("99"))
            }
        }
    }

    // ── DrawPlayerCards event ─────────────────────────────────────────────────

    @Test
    fun drawPlayerCardsEventAppearsInLog() {
        val state = stateAfterDraw()
        ActivityScenario.launch<GameLogActivity>(intentFor(state)).use { scenario ->
            scenario.onActivity { activity ->
                val container = activity.findViewById<LinearLayout>(R.id.eventLogContainer)
                // At least the section header was added
                assertTrue(container.childCount > 0)
                val header = container.getChildAt(0) as TextView
                assertTrue(header.text.contains("Turn 1"))
                assertTrue(header.text.contains("Medic"))
            }
        }
    }

    @Test
    fun drawEventIsNumberedCorrectly() {
        val state = stateAfterDraw()
        ActivityScenario.launch<GameLogActivity>(intentFor(state)).use { scenario ->
            scenario.onActivity { activity ->
                val container = activity.findViewById<LinearLayout>(R.id.eventLogContainer)
                val header = container.getChildAt(0) as TextView
                assertTrue("Header should say Turn 1", header.text.contains("Turn 1"))
            }
        }
    }

    // ── InfectionEvent ────────────────────────────────────────────────────────

    @Test
    fun infectionEventAppearsInLog() {
        val state = stateAfterInfect()
        ActivityScenario.launch<GameLogActivity>(intentFor(state)).use { scenario ->
            scenario.onActivity { activity ->
                val container = activity.findViewById<LinearLayout>(R.id.eventLogContainer)
                assertTrue(container.childCount > 0)
                val header = container.getChildAt(0) as TextView
                assertTrue(header.text.contains("Turn 1"))
            }
        }
    }

    @Test
    fun drawAndInfectFromSamePlayerShareOneTurnHeader() {
        val afterDraw = stateAfterDraw()
        val afterInfect = stateAfterInfect(afterDraw)
        assertEquals(2, afterInfect.eventLog.size)

        ActivityScenario.launch<GameLogActivity>(intentFor(afterInfect)).use { scenario ->
            scenario.onActivity { activity ->
                val container = activity.findViewById<LinearLayout>(R.id.eventLogContainer)
                val headers = (0 until container.childCount)
                    .map { container.getChildAt(it) }
                    .filterIsInstance<TextView>()
                    .map { it.text.toString() }
                // The draw and the infection that follows it belong to the same player's
                // turn, so they should be grouped under a single "Turn 1" header rather
                // than being numbered as two separate events.
                assertEquals(1, headers.count { it.contains("Turn 1") })
                assertTrue(headers.none { it.contains("Turn 2") })
            }
        }
    }

    @Test
    fun drawAndInfectionPhasesAreLabeledSeparately() {
        val afterDraw = stateAfterDraw()
        val afterInfect = stateAfterInfect(afterDraw)

        ActivityScenario.launch<GameLogActivity>(intentFor(afterInfect)).use { scenario ->
            scenario.onActivity { activity ->
                val headers = headerTexts(activity)
                assertTrue("Draw phase header should appear", headers.any { it.contains("Draw phase") })
                assertTrue("Infection phase header should appear", headers.any { it.contains("Infection phase") })
                assertTrue(
                    "Draw phase should come before Infection phase",
                    headers.indexOfFirst { it.contains("Draw phase") } <
                        headers.indexOfFirst { it.contains("Infection phase") }
                )
            }
        }
    }

    @Test
    fun dividerSeparatesTurnsButNotFirstTurn() {
        val afterFirstDraw = stateAfterDraw()
        val afterFirstInfect = stateAfterInfect(afterFirstDraw)
        val afterSecondDraw = (afterFirstInfect.executeTransition(Transition.DRAW_PLAYER_CARDS, rng)
                as TrackableState.TransitionResult.Success.DrawPlayerCardsTransitionResult)
            .newGameState

        ActivityScenario.launch<GameLogActivity>(intentFor(afterSecondDraw)).use { scenario ->
            scenario.onActivity { activity ->
                val container = activity.findViewById<LinearLayout>(R.id.eventLogContainer)
                val children = (0 until container.childCount).map { container.getChildAt(it) }
                val dividerCount = children.count { it !is TextView }
                assertEquals("There should be exactly one divider, between the two turns", 1, dividerCount)

                val firstHeaderIndex = children.indexOfFirst { it is TextView && (it as TextView).text.contains("Turn 1") }
                assertEquals("Nothing should precede the first turn's header", 0, firstHeaderIndex)
            }
        }
    }

    @Test
    fun secondPlayersTurnGetsItsOwnHeader() {
        // Simulate a full round: player 0 draws + infects, then player 1 draws.
        val afterFirstDraw = stateAfterDraw()
        val afterFirstInfect = stateAfterInfect(afterFirstDraw)
        val afterSecondDraw = (afterFirstInfect.executeTransition(Transition.DRAW_PLAYER_CARDS, rng)
                as TrackableState.TransitionResult.Success.DrawPlayerCardsTransitionResult)
            .newGameState
        assertEquals(3, afterSecondDraw.eventLog.size)

        ActivityScenario.launch<GameLogActivity>(intentFor(afterSecondDraw)).use { scenario ->
            scenario.onActivity { activity ->
                val container = activity.findViewById<LinearLayout>(R.id.eventLogContainer)
                val headers = (0 until container.childCount)
                    .map { container.getChildAt(it) }
                    .filterIsInstance<TextView>()
                    .map { it.text.toString() }
                assertTrue(headers.any { it.contains("Turn 1") && it.contains("Medic") })
                assertTrue(headers.any { it.contains("Turn 2") && it.contains("Scientist") })
            }
        }
    }

    // ── DrawPlayerCardsEvent with epidemic ────────────────────────────────────

    @Test
    fun drawPlayerCardsEventWithEpidemicAppearsInLog() {
        val epidemic = NamedEpidemic("Virulent")
        val city = cities[0]
        val event = GameEvent.DrawPlayerCardsEvent(
            cardsDrawn = listOf(epidemic, CityPlayerCard(cities[1])),
            epidemicsAndInfectedCities = listOf(epidemic to city),
            player = Player(Role("Medic"))
        )
        val state = makeState(eventLog = listOf(event))
        ActivityScenario.launch<GameLogActivity>(intentFor(state)).use { scenario ->
            scenario.onActivity { activity ->
                val container = activity.findViewById<LinearLayout>(R.id.eventLogContainer)
                val headers = (0 until container.childCount)
                    .map { container.getChildAt(it) }
                    .filterIsInstance<TextView>()
                    .map { it.text.toString() }
                assertTrue("Epidemic section should appear in log", headers.any { it.contains("Epidemic") })
            }
        }
    }

    // ── Initialization section ──────────────────────────────────────────────────

    @Test
    fun initializationSectionOmittedWhenInitialStateMissing() {
        // No INITIAL_STATE extra is set by intentFor() by default, so the log should
        // look exactly like it did before this feature: just the "No events yet" header.
        val state = makeState()
        ActivityScenario.launch<GameLogActivity>(intentFor(state)).use { scenario ->
            scenario.onActivity { activity ->
                val container = activity.findViewById<LinearLayout>(R.id.eventLogContainer)
                assertEquals(1, container.childCount)
            }
        }
    }

    @Test
    fun initializationSectionAppearsWhenInitialStateProvided() {
        val state = makeState()
        val initialState = makeInitialState(state.players)
        ActivityScenario.launch<GameLogActivity>(intentFor(state, initialState = initialState)).use { scenario ->
            scenario.onActivity { activity ->
                val headers = headerTexts(activity)
                assertTrue("Initialization header should appear", headers.any { it.contains("Initialization") })
            }
        }
    }

    @Test
    fun initializationSectionShowsStartingHandsForEachPlayer() {
        val state = makeState()
        val initialState = makeInitialState(state.players)
        ActivityScenario.launch<GameLogActivity>(intentFor(state, initialState = initialState)).use { scenario ->
            scenario.onActivity { activity ->
                val headers = headerTexts(activity)
                assertTrue("Medic starting hand header should appear", headers.any { it.contains("Medic starting hand") })
                assertTrue("Scientist starting hand header should appear", headers.any { it.contains("Scientist starting hand") })
            }
        }
    }

    @Test
    fun initializationSectionShowsCubeGroupings() {
        val state = makeState()
        val initialState = makeInitialState(state.players)
        ActivityScenario.launch<GameLogActivity>(intentFor(state, initialState = initialState)).use { scenario ->
            scenario.onActivity { activity ->
                val headers = headerTexts(activity)
                assertTrue("3 cube header should appear", headers.any { it.contains("3 cubes") })
                assertTrue("2 cube header should appear", headers.any { it.contains("2 cubes") })
                assertTrue("1 cube header should appear", headers.any { it.contains("1 cube") })
            }
        }
    }

    @Test
    fun initializationSectionAppearsBeforeEventLog() {
        val state = stateAfterDraw()
        val initialState = makeInitialState(state.players)
        ActivityScenario.launch<GameLogActivity>(intentFor(state, initialState = initialState)).use { scenario ->
            scenario.onActivity { activity ->
                val headers = headerTexts(activity)
                val initIndex = headers.indexOfFirst { it.contains("Initialization") }
                val eventIndex = headers.indexOfFirst { it.contains("Turn 1") }
                assertTrue("Initialization should come before the event log", initIndex in 0 until eventIndex)
            }
        }
    }

    // ── close button ──────────────────────────────────────────────────────────

    @Test
    fun closeButtonFinishesActivity() {
        ActivityScenario.launch<GameLogActivity>(intentFor(makeState())).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<android.view.View>(R.id.closeButton).performClick()
                assertTrue("activity should be finishing after close", activity.isFinishing)
            }
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun intentFor(state: TrackableState, seed: Long = 42L, initialState: UntrackableState? = null): Intent =
        Intent(ApplicationProvider.getApplicationContext(), GameLogActivity::class.java).apply {
            putExtra(GameLogActivity.GAME_STATE, state)
            putExtra(GameLogActivity.SEED, seed)
            initialState?.let { putExtra(GameLogActivity.INITIAL_STATE, it) }
        }

    private fun headerTexts(activity: GameLogActivity): List<String> {
        val container = activity.findViewById<LinearLayout>(R.id.eventLogContainer)
        return (0 until container.childCount)
            .map { container.getChildAt(it) }
            .filterIsInstance<TextView>()
            .map { it.text.toString() }
    }

    private fun makeInitialState(players: List<Player>): UntrackableState {
        val hands = players.associateWith { cities.take(4).map { c -> CityPlayerCard(c) }.toSet() }
        val cityStates = mapOf(
            cities[10] to CityState(mapOf(Color.BLUE to 3)),
            cities[11] to CityState(mapOf(Color.YELLOW to 2)),
            cities[12] to CityState(mapOf(Color.BLACK to 1))
        )
        return UntrackableState(BoardState(cityStates), hands)
    }

    private fun makeState(
        playerCards: List<PlayerCard> = cities.take(5).map { CityPlayerCard(it) },
        lastTransition: Transition = Transition.INFECT,
        eventLog: List<GameEvent> = emptyList()
    ): TrackableState {
        val players = listOf(Player(Role("Medic")), Player(Role("Scientist")))
        return TrackableState(
            curPlayer = 0,
            players = players,
            infectionDeck = Deck(cities.take(10).map { InfectionCard(it) }),
            infectionDiscardPile = cities.drop(10).map { InfectionCard(it) }.toSet(),
            playerDeck = Deck(playerCards),
            infectionRate = InfectionRate.INITIAL,
            lastTransition = lastTransition,
            eventLog = eventLog
        )
    }

    private fun stateAfterDraw(): TrackableState {
        val state = makeState(lastTransition = Transition.INFECT)
        return (state.executeTransition(Transition.DRAW_PLAYER_CARDS, rng)
                as TrackableState.TransitionResult.Success.DrawPlayerCardsTransitionResult)
            .newGameState
    }

    private fun stateAfterInfect(base: TrackableState = makeState(lastTransition = Transition.DRAW_PLAYER_CARDS)): TrackableState {
        return (base.executeTransition(Transition.INFECT, rng)
                as TrackableState.TransitionResult.Success.InfectionTransitionResult)
            .newGameState
    }
}
