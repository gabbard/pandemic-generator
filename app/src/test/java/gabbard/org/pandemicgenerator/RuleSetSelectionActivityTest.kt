package gabbard.org.pandemicgenerator

import android.content.Context
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import org.gabbard.pandemicgenerator.BUILT_IN_RULE_SETS
import org.gabbard.pandemicgenerator.NATIONAL_CHAMPIONSHIP
import org.junit.After
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
class RuleSetSelectionActivityTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @After
    fun tearDown() {
        context.deleteFile("custom_rule_sets.ser")
    }

    @Test
    fun builtInRuleSetsAreShownOnLaunch() {
        ActivityScenario.launch(RuleSetSelectionActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val container = activity.findViewById<LinearLayout>(R.id.rule_sets_container)
                assertEquals(BUILT_IN_RULE_SETS.size, container.childCount)
            }
        }
    }

    @Test
    fun builtInRuleSetNameIncludesBuiltInLabel() {
        ActivityScenario.launch(RuleSetSelectionActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val container = activity.findViewById<LinearLayout>(R.id.rule_sets_container)
                val firstRow = container.getChildAt(0) as LinearLayout
                val nameView = firstRow.getChildAt(0) as TextView
                assertTrue(
                    "First built-in row name should contain '(built-in)'",
                    nameView.text.contains("(built-in)")
                )
            }
        }
    }

    @Test
    fun selectButtonStartsGameOptionsActivity() {
        ActivityScenario.launch(RuleSetSelectionActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val container = activity.findViewById<LinearLayout>(R.id.rule_sets_container)
                val firstRow = container.getChildAt(0) as LinearLayout
                // Children: TextView (index 0), Button "Select" (index 1)
                val selectButton = firstRow.getChildAt(1) as Button
                selectButton.performClick()
                val started = shadowOf(activity).nextStartedActivity
                assertEquals(GameOptionsActivity::class.java.name, started.component?.className)
            }
        }
    }

    @Test
    fun selectButtonPassesRuleSetToGameOptionsActivity() {
        ActivityScenario.launch(RuleSetSelectionActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val container = activity.findViewById<LinearLayout>(R.id.rule_sets_container)
                val firstRow = container.getChildAt(0) as LinearLayout
                val selectButton = firstRow.getChildAt(1) as Button
                selectButton.performClick()
                val started = shadowOf(activity).nextStartedActivity
                @Suppress("DEPRECATION")
                val ruleSet = started.getSerializableExtra(RuleSetSelectionActivity.RULE_SET)
                assertNotNull("Started activity should receive a rule set extra", ruleSet)
            }
        }
    }

    @Test
    fun createCustomButtonStartsEditRuleSetActivity() {
        ActivityScenario.launch(RuleSetSelectionActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                activity.findViewById<Button>(R.id.create_custom_button).performClick()
                val started = shadowOf(activity).nextStartedActivity
                assertEquals(EditRuleSetActivity::class.java.name, started.component?.className)
            }
        }
    }

    @Test
    fun customRuleSetIsShownInList() {
        RuleSetRepository.save(context, listOf(NATIONAL_CHAMPIONSHIP))
        ActivityScenario.launch(RuleSetSelectionActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val container = activity.findViewById<LinearLayout>(R.id.rule_sets_container)
                // 2 built-in + 1 custom
                assertEquals(3, container.childCount)
            }
        }
    }

    @Test
    fun customRuleSetHasEditButton() {
        RuleSetRepository.save(context, listOf(NATIONAL_CHAMPIONSHIP))
        ActivityScenario.launch(RuleSetSelectionActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val container = activity.findViewById<LinearLayout>(R.id.rule_sets_container)
                // Custom row is at index 2 (after the 2 built-in rows)
                val customRow = container.getChildAt(2) as LinearLayout
                val buttonTexts = (0 until customRow.childCount)
                    .map { customRow.getChildAt(it) }
                    .filterIsInstance<Button>()
                    .map { it.text.toString() }
                assertTrue(
                    "Custom rule set row should have an 'Edit' button, found: $buttonTexts",
                    buttonTexts.any { it == "Edit" }
                )
            }
        }
    }

    @Test
    fun editButtonStartsEditRuleSetActivity() {
        RuleSetRepository.save(context, listOf(NATIONAL_CHAMPIONSHIP))
        ActivityScenario.launch(RuleSetSelectionActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val container = activity.findViewById<LinearLayout>(R.id.rule_sets_container)
                val customRow = container.getChildAt(2) as LinearLayout
                // Children: TextView(0), Select(1), Edit(2), Delete(3)
                val editButton = customRow.getChildAt(2) as Button
                editButton.performClick()
                val started = shadowOf(activity).nextStartedActivity
                assertEquals(EditRuleSetActivity::class.java.name, started.component?.className)
            }
        }
    }

    @Test
    fun deleteButtonRequiresConfirmationBeforeRemoving() {
        RuleSetRepository.save(context, listOf(NATIONAL_CHAMPIONSHIP))
        ActivityScenario.launch(RuleSetSelectionActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val container = activity.findViewById<LinearLayout>(R.id.rule_sets_container)
                val countBefore = container.childCount
                val customRow = container.getChildAt(2) as LinearLayout
                // Children: TextView(0), Select(1), Edit(2), Delete(3)
                val deleteButton = customRow.getChildAt(3) as Button
                deleteButton.performClick()
                // Dialog is shown but not confirmed — row count should be unchanged
                assertEquals("Delete should require confirmation, not remove immediately",
                    countBefore, container.childCount)
            }
        }
    }

    @Test
    fun customSelectButtonPassesRuleSet() {
        RuleSetRepository.save(context, listOf(NATIONAL_CHAMPIONSHIP))
        ActivityScenario.launch(RuleSetSelectionActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val container = activity.findViewById<LinearLayout>(R.id.rule_sets_container)
                val customRow = container.getChildAt(2) as LinearLayout
                // Children: TextView (index 0), Button "Select" (index 1), Button "Edit" (index 2), Button "Delete" (index 3)
                val selectButton = customRow.getChildAt(1) as Button
                selectButton.performClick()
                val started = shadowOf(activity).nextStartedActivity
                assertEquals(GameOptionsActivity::class.java.name, started.component?.className)
            }
        }
    }
}
