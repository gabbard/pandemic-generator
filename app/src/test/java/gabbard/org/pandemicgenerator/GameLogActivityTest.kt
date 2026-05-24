package gabbard.org.pandemicgenerator

import android.content.Intent
import android.widget.LinearLayout
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import org.gabbard.pandemicgenerator.ALL_CITIES
import org.gabbard.pandemicgenerator.CityPlayerCard
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
                assertTrue(header.text.contains("Drew Player Cards"))
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
                assertTrue("Header should say Event 1", header.text.contains("Event 1"))
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
                assertTrue(header.text.contains("Infection"))
            }
        }
    }

    @Test
    fun multipleEventsAreNumberedSequentially() {
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
                assertTrue(headers.any { it.contains("Event 1") })
                assertTrue(headers.any { it.contains("Event 2") })
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

    private fun intentFor(state: TrackableState, seed: Long = 42L): Intent =
        Intent(ApplicationProvider.getApplicationContext(), GameLogActivity::class.java).apply {
            putExtra(GameLogActivity.GAME_STATE, state)
            putExtra(GameLogActivity.SEED, seed)
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
