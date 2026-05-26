package gabbard.org.pandemicgenerator

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import org.gabbard.pandemicgenerator.MULTI_BOARD_COMPATIBLE_EVENTS
import org.gabbard.pandemicgenerator.NATIONAL_CHAMPIONSHIP
import org.gabbard.pandemicgenerator.RuleSet
import org.gabbard.pandemicgenerator.STANDARD_PANDEMIC_EVENTS
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class EditRuleSetActivityTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @After
    fun tearDown() {
        context.deleteFile("custom_rule_sets.ser")
    }

    private fun newIntent(): Intent =
        Intent(ApplicationProvider.getApplicationContext(), EditRuleSetActivity::class.java)

    private fun editIntent(ruleSet: RuleSet = NATIONAL_CHAMPIONSHIP, index: Int = 0): Intent =
        Intent(ApplicationProvider.getApplicationContext(), EditRuleSetActivity::class.java).apply {
            putExtra(EditRuleSetActivity.RULE_SET, ruleSet)
            putExtra(EditRuleSetActivity.CUSTOM_INDEX, index)
        }

    /** Check all role checkboxes in roles_container so validation passes. */
    private fun checkAllRoles(activity: EditRuleSetActivity) {
        val rolesContainer = activity.findViewById<LinearLayout>(R.id.roles_container)
        for (i in 0 until rolesContainer.childCount) {
            val child = rolesContainer.getChildAt(i)
            if (child is CheckBox) {
                child.isChecked = true
            }
        }
    }

    // ── New rule set defaults ─────────────────────────────────────────────────

    @Test
    fun newRuleSetHasDefaultPlayerCountsChecked() {
        ActivityScenario.launch<EditRuleSetActivity>(newIntent()).use { scenario ->
            scenario.onActivity { activity ->
                assertTrue(activity.findViewById<CheckBox>(R.id.player_count_2).isChecked)
                assertTrue(activity.findViewById<CheckBox>(R.id.player_count_3).isChecked)
                assertTrue(activity.findViewById<CheckBox>(R.id.player_count_4).isChecked)
            }
        }
    }

    @Test
    fun newRuleSetHasSimpleEpidemicSelected() {
        ActivityScenario.launch<EditRuleSetActivity>(newIntent()).use { scenario ->
            scenario.onActivity { activity ->
                assertTrue(activity.findViewById<RadioButton>(R.id.epidemic_simple).isChecked)
            }
        }
    }

    @Test
    fun namedEpidemicsContainerInitiallyGone() {
        ActivityScenario.launch<EditRuleSetActivity>(newIntent()).use { scenario ->
            scenario.onActivity { activity ->
                val container = activity.findViewById<View>(R.id.named_epidemics_container)
                assertEquals(View.GONE, container.visibility)
            }
        }
    }

    // ── Epidemic type toggle ──────────────────────────────────────────────────

    @Test
    fun selectingNamedEpidemicRevealsList() {
        ActivityScenario.launch<EditRuleSetActivity>(newIntent()).use { scenario ->
            scenario.onActivity { activity ->
                val radioGroup = activity.findViewById<RadioGroup>(R.id.epidemic_type_group)
                radioGroup.check(R.id.epidemic_named)
                val container = activity.findViewById<View>(R.id.named_epidemics_container)
                assertEquals(View.VISIBLE, container.visibility)
            }
        }
    }

    // ── Add difficulty button ─────────────────────────────────────────────────

    @Test
    fun addDifficultyButtonAddsDifficultyRow() {
        ActivityScenario.launch<EditRuleSetActivity>(newIntent()).use { scenario ->
            scenario.onActivity { activity ->
                val difficultiesContainer = activity.findViewById<LinearLayout>(R.id.difficulties_container)
                val countBefore = difficultiesContainer.childCount
                activity.findViewById<View>(R.id.add_difficulty_button).performClick()
                assertEquals(countBefore + 1, difficultiesContainer.childCount)
            }
        }
    }

    // ── Container population ──────────────────────────────────────────────────

    @Test
    fun eventsContainerIsPopulated() {
        ActivityScenario.launch<EditRuleSetActivity>(newIntent()).use { scenario ->
            scenario.onActivity { activity ->
                val eventsContainer = activity.findViewById<LinearLayout>(R.id.events_container)
                assertTrue(
                    "events_container should have at least one child",
                    eventsContainer.childCount > 0
                )
            }
        }
    }

    @Test
    fun newRuleSetDefaultsToStandardPandemicEvents() {
        val expectedChecked = (STANDARD_PANDEMIC_EVENTS intersect MULTI_BOARD_COMPATIBLE_EVENTS)
            .map { it.name }.toSet()
        ActivityScenario.launch<EditRuleSetActivity>(newIntent()).use { scenario ->
            scenario.onActivity { activity ->
                val eventsContainer = activity.findViewById<LinearLayout>(R.id.events_container)
                for (i in 0 until eventsContainer.childCount) {
                    val child = eventsContainer.getChildAt(i) as? CheckBox ?: continue
                    val shouldBeChecked = child.text.toString() in expectedChecked
                    assertEquals(
                        "Event '${child.text}' checked state wrong",
                        shouldBeChecked,
                        child.isChecked
                    )
                }
            }
        }
    }

    @Test
    fun rolesContainerIsPopulated() {
        ActivityScenario.launch<EditRuleSetActivity>(newIntent()).use { scenario ->
            scenario.onActivity { activity ->
                val rolesContainer = activity.findViewById<LinearLayout>(R.id.roles_container)
                assertTrue(
                    "roles_container should have at least one child",
                    rolesContainer.childCount > 0
                )
            }
        }
    }

    // ── Save validation ───────────────────────────────────────────────────────

    @Test
    fun saveWithEmptyNameDoesNotFinish() {
        ActivityScenario.launch<EditRuleSetActivity>(newIntent()).use { scenario ->
            scenario.onActivity { activity ->
                // Name is empty by default on a new rule set
                activity.findViewById<View>(R.id.save_button).performClick()
                assertFalse("Activity should not finish when name is empty", activity.isFinishing)
            }
        }
    }

    @Test
    fun saveValidNewRuleSetFinishesActivity() {
        ActivityScenario.launch<EditRuleSetActivity>(newIntent()).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<android.widget.EditText>(R.id.ruleset_name).setText("Test Rule Set")
                checkAllRoles(activity)
                activity.findViewById<android.widget.EditText>(R.id.num_events_to_use).setText("0")
                activity.findViewById<View>(R.id.save_button).performClick()
                assertTrue("Activity should finish after a valid save", activity.isFinishing)
            }
        }
    }

    @Test
    fun saveValidNewRuleSetPersistsToRepository() {
        ActivityScenario.launch<EditRuleSetActivity>(newIntent()).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<android.widget.EditText>(R.id.ruleset_name).setText("Test Rule Set")
                checkAllRoles(activity)
                activity.findViewById<android.widget.EditText>(R.id.num_events_to_use).setText("0")
                activity.findViewById<View>(R.id.save_button).performClick()
                assertEquals(1, RuleSetRepository.load(context).size)
            }
        }
    }

    @Test
    fun noPlayerCountsSelectedDoesNotSave() {
        ActivityScenario.launch<EditRuleSetActivity>(newIntent()).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<CheckBox>(R.id.player_count_2).isChecked = false
                activity.findViewById<CheckBox>(R.id.player_count_3).isChecked = false
                activity.findViewById<CheckBox>(R.id.player_count_4).isChecked = false
                activity.findViewById<android.widget.EditText>(R.id.ruleset_name).setText("No Players Rule Set")
                checkAllRoles(activity)
                activity.findViewById<View>(R.id.save_button).performClick()
                assertFalse(
                    "Activity should not finish when no player counts are selected",
                    activity.isFinishing
                )
            }
        }
    }

    // ── Delete difficulty row ─────────────────────────────────────────────────

    @Test
    fun deleteDifficultyRowRemovesRow() {
        ActivityScenario.launch<EditRuleSetActivity>(newIntent()).use { scenario ->
            scenario.onActivity { activity ->
                val container = activity.findViewById<LinearLayout>(R.id.difficulties_container)
                val countBefore = container.childCount
                val firstRow = container.getChildAt(0) as LinearLayout
                (firstRow.getChildAt(2) as Button).performClick()
                assertEquals(countBefore - 1, container.childCount)
            }
        }
    }

    // ── Save validation – difficulties ────────────────────────────────────────

    @Test
    fun saveWithNoDifficultiesDoesNotSave() {
        ActivityScenario.launch<EditRuleSetActivity>(newIntent()).use { scenario ->
            scenario.onActivity { activity ->
                val container = activity.findViewById<LinearLayout>(R.id.difficulties_container)
                for (i in 0 until container.childCount) {
                    val row = container.getChildAt(i) as LinearLayout
                    (row.getChildAt(0) as EditText).setText("")
                    (row.getChildAt(1) as EditText).setText("")
                }
                activity.findViewById<EditText>(R.id.ruleset_name).setText("No Difficulties")
                checkAllRoles(activity)
                activity.findViewById<View>(R.id.save_button).performClick()
                assertFalse("Activity should not finish when no valid difficulties exist", activity.isFinishing)
            }
        }
    }

    // ── Save validation – events ──────────────────────────────────────────────

    @Test
    fun saveWithTooManyEventsDoesNotSave() {
        ActivityScenario.launch<EditRuleSetActivity>(newIntent()).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<EditText>(R.id.ruleset_name).setText("Too Many Events")
                checkAllRoles(activity)
                activity.findViewById<EditText>(R.id.num_events_to_use).setText("999")
                activity.findViewById<View>(R.id.save_button).performClick()
                assertFalse("Activity should not finish when numEventsToUse > availableEvents", activity.isFinishing)
            }
        }
    }

    // ── Save validation – roles ───────────────────────────────────────────────

    @Test
    fun saveWithInsufficientRolesDoesNotSave() {
        ActivityScenario.launch<EditRuleSetActivity>(newIntent()).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<EditText>(R.id.ruleset_name).setText("Few Roles")
                val rolesContainer = activity.findViewById<LinearLayout>(R.id.roles_container)
                var checked = 0
                for (i in 0 until rolesContainer.childCount) {
                    val child = rolesContainer.getChildAt(i) as? CheckBox ?: continue
                    child.isChecked = checked < 2
                    checked++
                }
                activity.findViewById<EditText>(R.id.num_events_to_use).setText("0")
                activity.findViewById<View>(R.id.save_button).performClick()
                assertFalse("Activity should not finish with fewer roles than max players", activity.isFinishing)
            }
        }
    }

    // ── Named epidemics save ──────────────────────────────────────────────────

    @Test
    fun saveWithNamedEpidemicsSucceeds() {
        ActivityScenario.launch<EditRuleSetActivity>(newIntent()).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<EditText>(R.id.ruleset_name).setText("Named Epidemic Test")
                activity.findViewById<RadioGroup>(R.id.epidemic_type_group).check(R.id.epidemic_named)
                val namedContainer = activity.findViewById<LinearLayout>(R.id.named_epidemics_container)
                for (i in 0 until namedContainer.childCount) {
                    (namedContainer.getChildAt(i) as? CheckBox)?.isChecked = true
                }
                checkAllRoles(activity)
                activity.findViewById<EditText>(R.id.num_events_to_use).setText("0")
                activity.findViewById<View>(R.id.save_button).performClick()
                assertTrue("Activity should finish with valid named epidemics", activity.isFinishing)
            }
        }
    }

    // ── Existing rule set population ──────────────────────────────────────────

    @Test
    fun existingRuleSetPopulatesName() {
        ActivityScenario.launch<EditRuleSetActivity>(editIntent()).use { scenario ->
            scenario.onActivity { activity ->
                val nameField = activity.findViewById<android.widget.EditText>(R.id.ruleset_name)
                assertEquals("National Championship", nameField.text.toString())
            }
        }
    }

    @Test
    fun existingRuleSetWithNamedEpidemicsShowsNamedContainer() {
        // NATIONAL_CHAMPIONSHIP uses NamedEpidemic (CHAMPIONSHIP_VIRULENT_STRAIN_EPIDEMICS)
        ActivityScenario.launch<EditRuleSetActivity>(editIntent()).use { scenario ->
            scenario.onActivity { activity ->
                assertTrue(
                    "epidemic_named should be checked for NATIONAL_CHAMPIONSHIP",
                    activity.findViewById<RadioButton>(R.id.epidemic_named).isChecked
                )
                val container = activity.findViewById<View>(R.id.named_epidemics_container)
                assertEquals(
                    "named_epidemics_container should be VISIBLE for NATIONAL_CHAMPIONSHIP",
                    View.VISIBLE,
                    container.visibility
                )
            }
        }
    }

    @Test
    fun saveExistingRuleSetUpdatesRepository() {
        RuleSetRepository.save(context, listOf(NATIONAL_CHAMPIONSHIP))
        ActivityScenario.launch<EditRuleSetActivity>(editIntent(index = 0)).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<android.widget.EditText>(R.id.ruleset_name).setText("Updated")
                checkAllRoles(activity)
                activity.findViewById<android.widget.EditText>(R.id.num_events_to_use).setText("0")
                activity.findViewById<View>(R.id.save_button).performClick()
                val loaded = RuleSetRepository.load(context)
                assertEquals("Updated", loaded[0].name)
            }
        }
    }
}
