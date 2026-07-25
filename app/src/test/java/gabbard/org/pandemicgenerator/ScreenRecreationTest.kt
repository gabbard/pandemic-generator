package gabbard.org.pandemicgenerator

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import org.gabbard.pandemicgenerator.ALL_CITIES
import org.gabbard.pandemicgenerator.CityPlayerCard
import org.gabbard.pandemicgenerator.Deck
import org.gabbard.pandemicgenerator.InfectionCard
import org.gabbard.pandemicgenerator.InfectionRate
import org.gabbard.pandemicgenerator.NamedEpidemic
import org.gabbard.pandemicgenerator.Player
import org.gabbard.pandemicgenerator.PlayerCard
import org.gabbard.pandemicgenerator.Role
import org.gabbard.pandemicgenerator.TrackableState
import org.gabbard.pandemicgenerator.Transition
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.util.Random

/**
 * Regression tests for the rng-refresh bug (same family as InitialSetup's, see
 * InitialSetupTest.rotatingScreenDisplaysSameGameForSameSeed): DrawPlayerCards and
 * InfectionActivity both used to call TrackableState.executeTransition() unconditionally
 * in onCreate(). A rotation (or a process death that recreates the activity while it's
 * still in the back stack) redelivers the same Intent, which in-process carries the same
 * Random instance, so re-running the transition on recreation could draw a second,
 * different set of cards/cities.
 *
 * The divergence only actually shows up when the transition consumes rng: for
 * DRAW_PLAYER_CARDS that only happens when an epidemic is drawn (executeEpidemic()
 * shuffles the discard pile back into the deck); a plain card draw, and INFECT in
 * general, are pure functions of the (immutable) TrackableState and reproduce
 * identically no matter how many times they're re-run. The InfectionActivity guard is
 * therefore currently a consistency/defense-in-depth measure — these tests confirm it
 * doesn't regress behavior — rather than a reproduction of an observable bug, and would
 * become load-bearing the moment INFECT gains any rng-consuming logic (e.g. an event
 * card that reshuffles the infection deck).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ScreenRecreationTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val cities = ALL_CITIES.toList()

    @After
    fun tearDown() {
        GameRepository.clear(context)
    }

    private fun collectText(view: View): List<String> = when (view) {
        is TextView -> listOf(view.text.toString())
        is ViewGroup -> (0 until view.childCount).flatMap { collectText(view.getChildAt(it)) }
        else -> emptyList()
    }

    private fun makeTrackableState(
        playerCards: List<PlayerCard>,
        lastTransition: Transition
    ): TrackableState {
        val players = listOf(Player(Role("Medic")), Player(Role("Scientist")))
        return TrackableState(
            curPlayer = 0,
            players = players,
            infectionDeck = Deck(cities.take(10).map { InfectionCard(it) }),
            infectionDiscardPile = cities.drop(10).map { InfectionCard(it) }.toSet(),
            playerDeck = Deck(playerCards),
            infectionRate = InfectionRate.INITIAL,
            lastTransition = lastTransition
        )
    }

    // Includes an epidemic among the drawn cards: an epidemic is the only thing that makes
    // DRAW_PLAYER_CARDS consume rng (via executeEpidemic()'s deck shuffle), which is what
    // actually exposes the recreation bug — a plain card draw is deterministic and produces
    // identical output whether executeTransition() runs once or twice.
    private fun drawPlayerCardsIntent(
        playerCards: List<PlayerCard> = listOf(NamedEpidemic("Virulent Strain")) +
            cities.take(6).map { CityPlayerCard(it) }
    ): Intent = Intent(context, DrawPlayerCards::class.java).apply {
        putExtra(DrawPlayerCards.GAME_STATE, makeTrackableState(playerCards, Transition.INFECT))
        putExtra(DrawPlayerCards.RANDOM_SOURCE, Random(42))
        putExtra(DrawPlayerCards.SEED, 42L)
        putExtra(DrawPlayerCards.TURN_DURATION, TurnTimer.NO_TIMER)
    }

    private fun infectionActivityIntent(): Intent = Intent(context, InfectionActivity::class.java).apply {
        putExtra(
            InfectionActivity.GAME_STATE,
            makeTrackableState(cities.take(6).map { CityPlayerCard(it) }, Transition.DRAW_PLAYER_CARDS)
        )
        putExtra(InfectionActivity.RANDOM_SOURCE, Random(42))
        putExtra(InfectionActivity.SEED, 42L)
        putExtra(InfectionActivity.TURN_DURATION, TurnTimer.NO_TIMER)
    }

    @Test
    fun drawPlayerCardsShowsSameCardsAfterRecreate() {
        var before: List<String> = emptyList()
        var after: List<String> = emptyList()
        ActivityScenario.launch<DrawPlayerCards>(drawPlayerCardsIntent()).use { scenario ->
            scenario.onActivity { activity ->
                before = collectText(activity.findViewById(R.id.cardsContainer))
            }
            scenario.recreate()
            scenario.onActivity { activity ->
                after = collectText(activity.findViewById(R.id.cardsContainer))
            }
        }
        assertTrue("Sanity check: expected some cards to compare", before.isNotEmpty())
        assertEquals(before, after)
    }

    @Test
    fun drawPlayerCardsForwardsSameGameStateAfterRecreate() {
        var before: TrackableState? = null
        var after: TrackableState? = null
        ActivityScenario.launch<DrawPlayerCards>(drawPlayerCardsIntent()).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.proceedToInfectionPhase).performClick()
                @Suppress("DEPRECATION")
                before = shadowOf(activity).nextStartedActivity
                    .getSerializableExtra(InfectionActivity.GAME_STATE) as TrackableState
            }
            scenario.recreate()
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.proceedToInfectionPhase).performClick()
                @Suppress("DEPRECATION")
                after = shadowOf(activity).nextStartedActivity
                    .getSerializableExtra(InfectionActivity.GAME_STATE) as TrackableState
            }
        }
        assertEquals(before, after)
    }

    @Test
    fun exhaustedPlayerDeckStartsGameOverActivityAfterRecreate() {
        ActivityScenario.launch<DrawPlayerCards>(drawPlayerCardsIntent(playerCards = emptyList())).use { scenario ->
            scenario.onActivity { activity ->
                assertEquals(
                    GameOverActivity::class.java.name,
                    shadowOf(activity).nextStartedActivity.component?.className
                )
            }
            scenario.recreate()
            scenario.onActivity { activity ->
                assertEquals(
                    GameOverActivity::class.java.name,
                    shadowOf(activity).nextStartedActivity.component?.className
                )
            }
        }
    }

    @Test
    fun infectionActivityShowsSameInfectedCitiesAfterRecreate() {
        var before: List<String> = emptyList()
        var after: List<String> = emptyList()
        ActivityScenario.launch<InfectionActivity>(infectionActivityIntent()).use { scenario ->
            scenario.onActivity { activity ->
                before = collectText(activity.findViewById(R.id.infectedCities))
            }
            scenario.recreate()
            scenario.onActivity { activity ->
                after = collectText(activity.findViewById(R.id.infectedCities))
            }
        }
        assertTrue("Sanity check: expected some infected cities to compare", before.isNotEmpty())
        assertEquals(before, after)
    }

    @Test
    fun infectionActivityForwardsSameGameStateAfterRecreate() {
        var before: TrackableState? = null
        var after: TrackableState? = null
        ActivityScenario.launch<InfectionActivity>(infectionActivityIntent()).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.nextTurn).performClick()
                @Suppress("DEPRECATION")
                before = shadowOf(activity).nextStartedActivity
                    .getSerializableExtra(TurnTimer.GAME_STATE) as TrackableState
            }
            scenario.recreate()
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.nextTurn).performClick()
                @Suppress("DEPRECATION")
                after = shadowOf(activity).nextStartedActivity
                    .getSerializableExtra(TurnTimer.GAME_STATE) as TrackableState
            }
        }
        assertEquals(before, after)
    }
}
