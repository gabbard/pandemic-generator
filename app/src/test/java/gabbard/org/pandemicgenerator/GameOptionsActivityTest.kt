package gabbard.org.pandemicgenerator

import android.content.Intent
import android.widget.Spinner
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import org.gabbard.pandemicgenerator.NATIONAL_CHAMPIONSHIP
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class GameOptionsActivityTest {

    private fun intent(): Intent =
        Intent(ApplicationProvider.getApplicationContext(), GameOptionsActivity::class.java).apply {
            putExtra(RuleSetSelectionActivity.RULE_SET, NATIONAL_CHAMPIONSHIP)
        }

    @Test
    fun ruleSetNameIsDisplayed() {
        ActivityScenario.launch<GameOptionsActivity>(intent()).use { scenario ->
            scenario.onActivity { activity ->
                val nameView = activity.findViewById<TextView>(R.id.ruleset_name)
                assertEquals("National Championship", nameView.text.toString())
            }
        }
    }

    @Test
    fun playerCountSpinnerHasEntries() {
        ActivityScenario.launch<GameOptionsActivity>(intent()).use { scenario ->
            scenario.onActivity { activity ->
                val spinner = activity.findViewById<Spinner>(R.id.player_count_spinner)
                assertTrue("player count spinner should have at least one entry", spinner.adapter.count >= 1)
            }
        }
    }

    @Test
    fun difficultySpinnerHasEntries() {
        ActivityScenario.launch<GameOptionsActivity>(intent()).use { scenario ->
            scenario.onActivity { activity ->
                val spinner = activity.findViewById<Spinner>(R.id.difficulty_spinner)
                assertTrue("difficulty spinner should have at least one entry", spinner.adapter.count >= 1)
            }
        }
    }

    @Test
    fun playerCountSpinnerShowsPlayerCountText() {
        ActivityScenario.launch<GameOptionsActivity>(intent()).use { scenario ->
            scenario.onActivity { activity ->
                val spinner = activity.findViewById<Spinner>(R.id.player_count_spinner)
                val first = spinner.adapter.getItem(0).toString()
                assertTrue("spinner item should contain 'players'", first.contains("players"))
            }
        }
    }

    @Test
    fun startGameButtonStartsEnterRandomSeed() {
        ActivityScenario.launch<GameOptionsActivity>(intent()).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<android.view.View>(R.id.start_game_button).performClick()
                val started = shadowOf(activity).nextStartedActivity
                assertEquals(EnterRandomSeed::class.java.name, started.component?.className)
            }
        }
    }

    @Test
    fun startGameButtonPassesGameRules() {
        ActivityScenario.launch<GameOptionsActivity>(intent()).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<android.view.View>(R.id.start_game_button).performClick()
                val started = shadowOf(activity).nextStartedActivity
                @Suppress("DEPRECATION")
                val gameRules = started.getSerializableExtra(EnterRandomSeed.GAME_RULES)
                assertNotNull("GAME_RULES extra should be passed to EnterRandomSeed", gameRules)
            }
        }
    }
}
