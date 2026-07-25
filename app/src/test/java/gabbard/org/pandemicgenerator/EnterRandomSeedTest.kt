package gabbard.org.pandemicgenerator

import android.content.Context
import android.content.Intent
import android.widget.EditText
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import org.gabbard.pandemicgenerator.NATIONAL_CHAMPIONSHIP_RULES
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowAlertDialog

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class EnterRandomSeedTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @After
    fun tearDown() {
        SeedHistory.clear(context)
    }

    private fun intent(): Intent =
        Intent(ApplicationProvider.getApplicationContext(), EnterRandomSeed::class.java).apply {
            putExtra(EnterRandomSeed.GAME_RULES, NATIONAL_CHAMPIONSHIP_RULES)
        }

    // ── random seed button ────────────────────────────────────────────────────

    @Test
    fun randomSeedButtonFillsSeedField() {
        ActivityScenario.launch<EnterRandomSeed>(intent()).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<android.view.View>(R.id.randomSeedButton).performClick()
                val text = activity.findViewById<EditText>(R.id.startGame).text.toString()
                assertFalse("seed field should not be empty after clicking Random Seed", text.isEmpty())
            }
        }
    }

    @Test
    fun randomSeedIsInValidRange() {
        ActivityScenario.launch<EnterRandomSeed>(intent()).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<android.view.View>(R.id.randomSeedButton).performClick()
                val seed = activity.findViewById<EditText>(R.id.startGame).text.toString().toLong()
                assertTrue("seed should be non-negative", seed >= 0L)
                assertTrue("seed should be < 1_000_000", seed < 1_000_000L)
            }
        }
    }

    @Test
    fun repeatedClicksProduceDifferentSeeds() {
        ActivityScenario.launch<EnterRandomSeed>(intent()).use { scenario ->
            val seeds = mutableSetOf<String>()
            scenario.onActivity { activity ->
                val btn = activity.findViewById<android.view.View>(R.id.randomSeedButton)
                val field = activity.findViewById<EditText>(R.id.startGame)
                repeat(10) {
                    btn.performClick()
                    seeds.add(field.text.toString())
                }
            }
            // With 10 clicks over a 0..999999 range, virtually guaranteed to get >1 distinct value
            assertTrue("repeated clicks should produce varied seeds", seeds.size > 1)
        }
    }

    // ── start game navigation ─────────────────────────────────────────────────

    @Test
    fun startGameNavigatesToInitialSetup() {
        ActivityScenario.launch<EnterRandomSeed>(intent()).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<EditText>(R.id.startGame).setText("12345")
                activity.findViewById<android.view.View>(R.id.button2).performClick()
                val started = shadowOf(activity).nextStartedActivity
                assertEquals(InitialSetup::class.java.name, started.component?.className)
            }
        }
    }

    @Test
    fun startGamePassesCorrectSeedToInitialSetup() {
        ActivityScenario.launch<EnterRandomSeed>(intent()).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<EditText>(R.id.startGame).setText("99999")
                activity.findViewById<android.view.View>(R.id.button2).performClick()
                val started = shadowOf(activity).nextStartedActivity
                assertEquals(99999L, started.getLongExtra(InitialSetup.SEED, -1L))
            }
        }
    }

    @Test
    fun startGamePassesGameRulesToInitialSetup() {
        ActivityScenario.launch<EnterRandomSeed>(intent()).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<EditText>(R.id.startGame).setText("42")
                activity.findViewById<android.view.View>(R.id.button2).performClick()
                val started = shadowOf(activity).nextStartedActivity
                @Suppress("DEPRECATION")
                val rules = started.getSerializableExtra(InitialSetup.GAME_RULES)
                assertEquals(NATIONAL_CHAMPIONSHIP_RULES, rules)
            }
        }
    }

    // ── recent seeds ───────────────────────────────────────────────────────────

    @Test
    fun startGameRecordsSeedInHistory() {
        ActivityScenario.launch<EnterRandomSeed>(intent()).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<EditText>(R.id.startGame).setText("54321")
                activity.findViewById<android.view.View>(R.id.button2).performClick()
            }
        }
        assertEquals(listOf(54321L), SeedHistory.recentSeeds(context))
    }

    @Test
    fun recentSeedsButtonShowsEmptyStateWhenNoHistory() {
        ActivityScenario.launch<EnterRandomSeed>(intent()).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<android.view.View>(R.id.recentSeedsButton).performClick()
                val dialog = ShadowAlertDialog.getLatestAlertDialog()
                assertEquals("No recent seeds yet.", shadowOf(dialog).message)
            }
        }
    }

    @Test
    fun recentSeedsButtonShowsHistoryWhenPresent() {
        SeedHistory.record(context, 111L)
        SeedHistory.record(context, 222L)
        ActivityScenario.launch<EnterRandomSeed>(intent()).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<android.view.View>(R.id.recentSeedsButton).performClick()
                val dialog = ShadowAlertDialog.getLatestAlertDialog()
                val items = shadowOf(dialog).items.map { it.toString() }
                assertEquals(listOf("222", "111"), items)
            }
        }
    }

    @Test
    fun selectingRecentSeedPopulatesSeedField() {
        SeedHistory.record(context, 777L)
        ActivityScenario.launch<EnterRandomSeed>(intent()).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<android.view.View>(R.id.recentSeedsButton).performClick()
                shadowOf(ShadowAlertDialog.getLatestAlertDialog()).clickOnItem(0)
                val text = activity.findViewById<EditText>(R.id.startGame).text.toString()
                assertEquals("777", text)
            }
        }
    }
}
