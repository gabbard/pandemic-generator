package gabbard.org.pandemicgenerator

import android.content.Intent
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import org.gabbard.pandemicgenerator.ALL_CITIES
import org.gabbard.pandemicgenerator.CityPlayerCard
import org.gabbard.pandemicgenerator.Deck
import org.gabbard.pandemicgenerator.InfectionCard
import org.gabbard.pandemicgenerator.InfectionRate
import org.gabbard.pandemicgenerator.Player
import org.gabbard.pandemicgenerator.PlayerCard
import org.gabbard.pandemicgenerator.Role
import org.gabbard.pandemicgenerator.TrackableState
import org.gabbard.pandemicgenerator.Transition
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.util.Random

/**
 * Verifies that each screen navigates to the correct next screen when the user taps
 * the action button.  Uses Robolectric so these run on the JVM in CI without a device.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class NavigationTest {

    private val cities = ALL_CITIES.toList()

    private fun makeTrackableState(
        playerCards: List<PlayerCard> = cities.take(5).map { CityPlayerCard(it) },
        lastTransition: Transition = Transition.INFECT
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

    private fun drawPlayerCardsIntent(
        playerCards: List<PlayerCard> = cities.take(5).map { CityPlayerCard(it) }
    ): Intent = Intent(
        ApplicationProvider.getApplicationContext(),
        DrawPlayerCards::class.java
    ).apply {
        putExtra(DrawPlayerCards.GAME_STATE, makeTrackableState(playerCards = playerCards))
        putExtra(DrawPlayerCards.RANDOM_SOURCE, Random(42))
        putExtra(DrawPlayerCards.SEED, 42L)
        putExtra(DrawPlayerCards.TURN_DURATION, TurnTimer.NO_TIMER)
    }

    private fun infectionActivityIntent(): Intent = Intent(
        ApplicationProvider.getApplicationContext(),
        InfectionActivity::class.java
    ).apply {
        putExtra(
            InfectionActivity.GAME_STATE,
            makeTrackableState(lastTransition = Transition.DRAW_PLAYER_CARDS)
        )
        putExtra(InfectionActivity.RANDOM_SOURCE, Random(42))
        putExtra(InfectionActivity.SEED, 42L)
        putExtra(InfectionActivity.TURN_DURATION, TurnTimer.NO_TIMER)
    }

    @Test
    fun proceedToInfectionPhaseButtonStartsInfectionActivity() {
        ActivityScenario.launch<DrawPlayerCards>(drawPlayerCardsIntent()).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.proceedToInfectionPhase).performClick()
                val started = shadowOf(activity).nextStartedActivity
                assertEquals(InfectionActivity::class.java.name, started.component?.className)
            }
        }
    }

    @Test
    fun drawPlayerCardsPassesStateWithDrawTransitionToInfectionActivity() {
        ActivityScenario.launch<DrawPlayerCards>(drawPlayerCardsIntent()).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.proceedToInfectionPhase).performClick()
                val started = shadowOf(activity).nextStartedActivity
                @Suppress("DEPRECATION")
                val passedState = started.getSerializableExtra(InfectionActivity.GAME_STATE)
                        as TrackableState
                assertEquals(Transition.DRAW_PLAYER_CARDS, passedState.lastTransition)
            }
        }
    }

    @Test
    fun exhaustedPlayerDeckStartsGameOverActivity() {
        // DrawPlayerCards.onCreate calls executeTransition immediately; an empty deck
        // triggers the PlayerDeckExhausted branch which starts GameOverActivity before
        // the layout is fully set up, so we just check the started intent.
        ActivityScenario.launch<DrawPlayerCards>(drawPlayerCardsIntent(playerCards = emptyList())).use { scenario ->
            scenario.onActivity { activity ->
                val started = shadowOf(activity).nextStartedActivity
                assertEquals(GameOverActivity::class.java.name, started.component?.className)
            }
        }
    }

    @Test
    fun nextTurnButtonStartsTurnTimer() {
        ActivityScenario.launch<InfectionActivity>(infectionActivityIntent()).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.nextTurn).performClick()
                val started = shadowOf(activity).nextStartedActivity
                assertEquals(TurnTimer::class.java.name, started.component?.className)
            }
        }
    }

    @Test
    fun infectionActivityPassesStateWithInfectTransitionToTurnTimer() {
        ActivityScenario.launch<InfectionActivity>(infectionActivityIntent()).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.nextTurn).performClick()
                val started = shadowOf(activity).nextStartedActivity
                @Suppress("DEPRECATION")
                val passedState = started.getSerializableExtra(TurnTimer.GAME_STATE)
                        as TrackableState
                assertEquals(Transition.INFECT, passedState.lastTransition)
            }
        }
    }
}
