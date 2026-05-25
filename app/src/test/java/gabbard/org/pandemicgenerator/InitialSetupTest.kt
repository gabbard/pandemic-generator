package gabbard.org.pandemicgenerator

import android.content.Intent
import android.widget.LinearLayout
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import org.gabbard.pandemicgenerator.NATIONAL_CHAMPIONSHIP_RULES
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import java.util.Random

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class InitialSetupTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()

    @After
    fun tearDown() {
        GameRepository.clear(context)
    }

    private fun intent(): Intent =
        Intent(ApplicationProvider.getApplicationContext(), InitialSetup::class.java).apply {
            putExtra(InitialSetup.GAME_RULES, NATIONAL_CHAMPIONSHIP_RULES)
            putExtra(InitialSetup.RANDOM_SOURCE, Random(42))
            putExtra(InitialSetup.SEED, 42L)
        }

    @Test
    fun seedIsDisplayedOnScreen() {
        ActivityScenario.launch<InitialSetup>(intent()).use { scenario ->
            scenario.onActivity { activity ->
                val seedView = activity.findViewById<TextView>(R.id.seedDisplay)
                assertTrue("seedDisplay should contain seed 42", seedView.text.contains("42"))
            }
        }
    }

    @Test
    fun player1CardsContainerIsPopulated() {
        ActivityScenario.launch<InitialSetup>(intent()).use { scenario ->
            scenario.onActivity { activity ->
                val container = activity.findViewById<LinearLayout>(R.id.player1Cards)
                assertTrue("player1Cards should have at least one child", container.childCount >= 1)
            }
        }
    }

    @Test
    fun player2CardsContainerIsPopulated() {
        ActivityScenario.launch<InitialSetup>(intent()).use { scenario ->
            scenario.onActivity { activity ->
                val container = activity.findViewById<LinearLayout>(R.id.player2Cards)
                assertTrue("player2Cards should have at least one child", container.childCount >= 1)
            }
        }
    }

    @Test
    fun boardCitiesContainerIsPopulated() {
        ActivityScenario.launch<InitialSetup>(intent()).use { scenario ->
            scenario.onActivity { activity ->
                val container = activity.findViewById<LinearLayout>(R.id.boardCities)
                assertTrue("boardCities should have at least one child", container.childCount >= 1)
            }
        }
    }

    @Test
    fun readyToPlayStartsTurnTimer() {
        ActivityScenario.launch<InitialSetup>(intent()).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<android.view.View>(R.id.button3).performClick()
                val started = shadowOf(activity).nextStartedActivity
                assertEquals(TurnTimer::class.java.name, started.component?.className)
            }
        }
    }

    @Test
    fun readyToPlayPassesGameStateToTurnTimer() {
        ActivityScenario.launch<InitialSetup>(intent()).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<android.view.View>(R.id.button3).performClick()
                val started = shadowOf(activity).nextStartedActivity
                @Suppress("DEPRECATION")
                val gameState = started.getSerializableExtra(TurnTimer.GAME_STATE)
                assertNotNull("GAME_STATE should be passed to TurnTimer", gameState)
            }
        }
    }

    @Test
    fun readyToPlayPassesSeedToTurnTimer() {
        ActivityScenario.launch<InitialSetup>(intent()).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<android.view.View>(R.id.button3).performClick()
                val started = shadowOf(activity).nextStartedActivity
                assertEquals(42L, started.getLongExtra(TurnTimer.SEED, -1L))
            }
        }
    }
}
