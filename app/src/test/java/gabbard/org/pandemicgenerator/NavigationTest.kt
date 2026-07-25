package gabbard.org.pandemicgenerator

import android.content.Context
import android.content.Intent
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import org.gabbard.pandemicgenerator.ALL_CITIES
import org.gabbard.pandemicgenerator.BoardState
import org.gabbard.pandemicgenerator.CityPlayerCard
import org.gabbard.pandemicgenerator.CityState
import org.gabbard.pandemicgenerator.Color
import org.gabbard.pandemicgenerator.Deck
import org.gabbard.pandemicgenerator.EventCard
import org.gabbard.pandemicgenerator.InfectionCard
import org.gabbard.pandemicgenerator.InfectionRate
import org.gabbard.pandemicgenerator.NamedEpidemic
import org.gabbard.pandemicgenerator.Player
import org.gabbard.pandemicgenerator.PlayerCard
import org.gabbard.pandemicgenerator.Role
import org.gabbard.pandemicgenerator.TrackableState
import org.gabbard.pandemicgenerator.Transition
import org.gabbard.pandemicgenerator.UntrackableState
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.fakes.RoboMenuItem
import org.robolectric.shadows.ShadowAlertDialog
import java.util.Random

/**
 * Verifies that each screen navigates to the correct next screen when the user taps
 * the action button.  Uses Robolectric so these run on the JVM in CI without a device.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class NavigationTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val cities = ALL_CITIES.toList()

    @After
    fun tearDown() {
        GameRepository.clear(context)
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

    // ── TurnTimer ─────────────────────────────────────────────────────────────

    @Test
    fun turnTimerDrawPlayerCardsButtonStartsDrawPlayerCards() {
        ActivityScenario.launch<TurnTimer>(turnTimerIntent()).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.drawPlayerCards).performClick()
                val started = shadowOf(activity).nextStartedActivity
                assertEquals(DrawPlayerCards::class.java.name, started.component?.className)
            }
        }
    }

    @Test
    fun turnTimerDisplaysCurrentPlayerRole() {
        ActivityScenario.launch<TurnTimer>(turnTimerIntent(roleName = "Medic")).use { scenario ->
            scenario.onActivity { activity ->
                val roleView = activity.findViewById<android.widget.TextView>(R.id.currentPlayerRole)
                assertEquals("Medic", roleView.text.toString())
            }
        }
    }

    @Test
    fun turnTimerPassesGameStateToDrawPlayerCards() {
        ActivityScenario.launch<TurnTimer>(turnTimerIntent()).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.drawPlayerCards).performClick()
                val started = shadowOf(activity).nextStartedActivity
                @Suppress("DEPRECATION")
                val passedState = started.getSerializableExtra(DrawPlayerCards.GAME_STATE)
                assertNotNull(passedState)
            }
        }
    }

    // ── MainActivity ──────────────────────────────────────────────────────────

    @Test
    fun resumeButtonHiddenWhenNoSavedGame() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val btn = activity.findViewById<View>(R.id.resumeGame)
                assertEquals(View.GONE, btn.visibility)
            }
        }
    }

    @Test
    fun resumeButtonVisibleWhenSavedGameExists() {
        GameRepository.save(context, makeGameSession())
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val btn = activity.findViewById<View>(R.id.resumeGame)
                assertEquals(View.VISIBLE, btn.visibility)
            }
        }
    }

    // ── GameOverActivity ──────────────────────────────────────────────────────

    @Test
    fun returnToMainMenuButtonStartsMainActivity() {
        ActivityScenario.launch(GameOverActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.returnToMainMenu).performClick()
                val started = shadowOf(activity).nextStartedActivity
                assertEquals(MainActivity::class.java.name, started.component?.className)
            }
        }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun turnTimerIntent(roleName: String = "Medic", withInitialState: Boolean = false): Intent =
        Intent(context, TurnTimer::class.java).apply {
            putExtra(TurnTimer.GAME_STATE, makeTrackableState(roleName = roleName))
            putExtra(TurnTimer.RANDOM_SOURCE, Random(42))
            putExtra(TurnTimer.SEED, 42L)
            putExtra(TurnTimer.TURN_DURATION, TurnTimer.NO_TIMER)
            if (withInitialState) {
                putExtra(TurnTimer.INITIAL_STATE, makeUntrackableState(roleName = roleName))
            }
        }

    private fun makeUntrackableState(roleName: String = "Medic"): UntrackableState {
        val players = listOf(Player(Role(roleName)), Player(Role("Scientist")))
        val hands = players.associateWith { cities.take(4).map { c -> CityPlayerCard(c) }.toSet() }
        val cityStates = mapOf(
            cities[10] to CityState(mapOf(Color.BLUE to 3)),
            cities[11] to CityState(mapOf(Color.YELLOW to 2)),
            cities[12] to CityState(mapOf(Color.BLACK to 1))
        )
        return UntrackableState(BoardState(cityStates), hands)
    }

    // ── MainActivity resume-game click ────────────────────────────────────────

    @Test
    fun resumeGameClickNavigatesToTurnTimer() {
        GameRepository.save(context, makeGameSession())
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.resumeGame).performClick()
                val started = shadowOf(activity).nextStartedActivity
                assertEquals(TurnTimer::class.java.name, started.component?.className)
            }
        }
    }

    @Test
    fun resumeGameClickPassesSavedStateToTurnTimer() {
        val session = makeGameSession()
        GameRepository.save(context, session)
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.resumeGame).performClick()
                val started = shadowOf(activity).nextStartedActivity
                @Suppress("DEPRECATION")
                val passedState = started.getSerializableExtra(TurnTimer.GAME_STATE) as TrackableState
                assertEquals(session.trackableState, passedState)
            }
        }
    }

    // ── GameActivity options menu ──────────────────────────────────────────────

    @Test
    fun viewLogMenuItemStartsGameLogActivity() {
        ActivityScenario.launch<TurnTimer>(turnTimerIntent()).use { scenario ->
            scenario.onActivity { activity ->
                activity.onOptionsItemSelected(RoboMenuItem(R.id.action_view_log))
                val started = shadowOf(activity).nextStartedActivity
                assertEquals(GameLogActivity::class.java.name, started.component?.className)
            }
        }
    }

    @Test
    fun viewLogMenuItemPassesCurrentStateToGameLogActivity() {
        ActivityScenario.launch<TurnTimer>(turnTimerIntent()).use { scenario ->
            scenario.onActivity { activity ->
                activity.onOptionsItemSelected(RoboMenuItem(R.id.action_view_log))
                val started = shadowOf(activity).nextStartedActivity
                @Suppress("DEPRECATION")
                assertNotNull(started.getSerializableExtra(GameLogActivity.GAME_STATE))
            }
        }
    }

    @Test
    fun endGameMenuItemShowsConfirmationDialog() {
        ActivityScenario.launch<TurnTimer>(turnTimerIntent()).use { scenario ->
            scenario.onActivity { activity ->
                activity.onOptionsItemSelected(RoboMenuItem(R.id.action_end_game))
                assertNotNull("End Game dialog should appear", ShadowAlertDialog.getLatestAlertDialog())
            }
        }
    }

    @Test
    fun turnTimerWithPositiveDurationShowsTimerView() {
        val intent = turnTimerIntent().apply { putExtra(TurnTimer.TURN_DURATION, 60) }
        ActivityScenario.launch<TurnTimer>(intent).use { scenario ->
            scenario.onActivity { activity ->
                val timerView = activity.findViewById<android.view.View>(R.id.timeRemaining)
                assertEquals(android.view.View.VISIBLE, timerView.visibility)
            }
        }
    }

    @Test
    fun viewLogMenuItemDoesNotStartActivityWhenStateIsNull() {
        // GameOverActivity extends GameActivity but does not override gameStateForLog(),
        // so it returns null — the action_view_log branch should be a no-op.
        ActivityScenario.launch(GameOverActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.onOptionsItemSelected(RoboMenuItem(R.id.action_view_log))
                val started = shadowOf(activity).nextStartedActivity
                assertEquals("No activity should start when state is null", null, started)
            }
        }
    }

    @Test
    fun unknownMenuItemDelegatesToSuper() {
        ActivityScenario.launch<TurnTimer>(turnTimerIntent()).use { scenario ->
            scenario.onActivity { activity ->
                // An unrecognized menu item ID should not crash; the 'else' branch delegates to super.
                val handled = activity.onOptionsItemSelected(RoboMenuItem(Int.MAX_VALUE))
                // super returns false for unknown items
                assertEquals(false, handled)
            }
        }
    }

    // ── DrawPlayerCards epidemic / event card paths ───────────────────────────

    @Test
    fun drawPlayerCardsWithEpidemicShowsEpidemicSection() {
        val playerCards = listOf(
            NamedEpidemic("Virulent Strain"),
            CityPlayerCard(cities[0]),
            CityPlayerCard(cities[1])
        )
        ActivityScenario.launch<DrawPlayerCards>(drawPlayerCardsIntent(playerCards = playerCards)).use { scenario ->
            scenario.onActivity { activity ->
                val container = activity.findViewById<android.widget.LinearLayout>(R.id.cardsContainer)
                // header + 2 card rows + epidemic header + city row = 5 min
                assertTrue("Epidemic section should appear", container.childCount >= 4)
            }
        }
    }

    @Test
    fun drawPlayerCardsWithEventCardIsDisplayed() {
        val playerCards = listOf(
            EventCard("Airlift"),
            CityPlayerCard(cities[0]),
            CityPlayerCard(cities[1])
        )
        ActivityScenario.launch<DrawPlayerCards>(drawPlayerCardsIntent(playerCards = playerCards)).use { scenario ->
            scenario.onActivity { activity ->
                val container = activity.findViewById<android.widget.LinearLayout>(R.id.cardsContainer)
                assertTrue("Event card row should be visible", container.childCount >= 3)
            }
        }
    }

    // ── MainActivity new game button ──────────────────────────────────────────

    @Test
    fun newGameButtonStartsRuleSetSelectionActivity() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.newGame).performClick()
                val started = shadowOf(activity).nextStartedActivity
                assertEquals(RuleSetSelectionActivity::class.java.name, started.component?.className)
            }
        }
    }

    // ── gameStateForLog / seedForLog coverage ─────────────────────────────────

    @Test
    fun viewLogMenuItemFromInfectionActivityPassesState() {
        ActivityScenario.launch<InfectionActivity>(infectionActivityIntent()).use { scenario ->
            scenario.onActivity { activity ->
                activity.onOptionsItemSelected(RoboMenuItem(R.id.action_view_log))
                val started = shadowOf(activity).nextStartedActivity
                @Suppress("DEPRECATION")
                assertNotNull(started?.getSerializableExtra(GameLogActivity.GAME_STATE))
            }
        }
    }

    @Test
    fun viewLogMenuItemFromDrawPlayerCardsPassesState() {
        ActivityScenario.launch<DrawPlayerCards>(drawPlayerCardsIntent()).use { scenario ->
            scenario.onActivity { activity ->
                activity.onOptionsItemSelected(RoboMenuItem(R.id.action_view_log))
                val started = shadowOf(activity).nextStartedActivity
                @Suppress("DEPRECATION")
                assertNotNull(started?.getSerializableExtra(GameLogActivity.GAME_STATE))
            }
        }
    }

    // ── INITIAL_STATE threading through the activity chain ─────────────────────

    @Test
    fun turnTimerForwardsInitialStateToDrawPlayerCards() {
        ActivityScenario.launch<TurnTimer>(turnTimerIntent(withInitialState = true)).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.drawPlayerCards).performClick()
                val started = shadowOf(activity).nextStartedActivity
                @Suppress("DEPRECATION")
                assertNotNull(
                    "INITIAL_STATE should be forwarded to DrawPlayerCards",
                    started.getSerializableExtra(DrawPlayerCards.INITIAL_STATE)
                )
            }
        }
    }

    @Test
    fun drawPlayerCardsForwardsInitialStateToInfectionActivity() {
        val intent = drawPlayerCardsIntent().apply {
            putExtra(DrawPlayerCards.INITIAL_STATE, makeUntrackableState())
        }
        ActivityScenario.launch<DrawPlayerCards>(intent).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.proceedToInfectionPhase).performClick()
                val started = shadowOf(activity).nextStartedActivity
                @Suppress("DEPRECATION")
                assertNotNull(
                    "INITIAL_STATE should be forwarded to InfectionActivity",
                    started.getSerializableExtra(InfectionActivity.INITIAL_STATE)
                )
            }
        }
    }

    @Test
    fun infectionActivityForwardsInitialStateToTurnTimer() {
        val intent = infectionActivityIntent().apply {
            putExtra(InfectionActivity.INITIAL_STATE, makeUntrackableState())
        }
        ActivityScenario.launch<InfectionActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<View>(R.id.nextTurn).performClick()
                val started = shadowOf(activity).nextStartedActivity
                @Suppress("DEPRECATION")
                assertNotNull(
                    "INITIAL_STATE should be forwarded back to TurnTimer",
                    started.getSerializableExtra(TurnTimer.INITIAL_STATE)
                )
            }
        }
    }

    @Test
    fun viewLogMenuItemFromTurnTimerIncludesInitialStateWhenAvailable() {
        ActivityScenario.launch<TurnTimer>(turnTimerIntent(withInitialState = true)).use { scenario ->
            scenario.onActivity { activity ->
                activity.onOptionsItemSelected(RoboMenuItem(R.id.action_view_log))
                val started = shadowOf(activity).nextStartedActivity
                @Suppress("DEPRECATION")
                assertNotNull(
                    "INITIAL_STATE should be passed to GameLogActivity when available",
                    started?.getSerializableExtra(GameLogActivity.INITIAL_STATE)
                )
            }
        }
    }

    @Test
    fun viewLogMenuItemOmitsInitialStateWhenUnavailable() {
        // The default turnTimerIntent() (as used by a resumed saved game) carries no
        // INITIAL_STATE extra, so the Game Log should simply not receive one either.
        ActivityScenario.launch<TurnTimer>(turnTimerIntent()).use { scenario ->
            scenario.onActivity { activity ->
                activity.onOptionsItemSelected(RoboMenuItem(R.id.action_view_log))
                val started = shadowOf(activity).nextStartedActivity
                @Suppress("DEPRECATION")
                assertEquals(null, started?.getSerializableExtra(GameLogActivity.INITIAL_STATE))
            }
        }
    }

    private fun makeGameSession(): GameRepository.GameSession =
        GameRepository.GameSession(
            trackableState = makeTrackableState(),
            rng = Random(42),
            seed = 42L
        )

    private fun makeTrackableState(
        playerCards: List<PlayerCard> = cities.take(5).map { CityPlayerCard(it) },
        lastTransition: Transition = Transition.INFECT,
        roleName: String = "Medic"
    ): TrackableState {
        val players = listOf(Player(Role(roleName)), Player(Role("Scientist")))
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
}
