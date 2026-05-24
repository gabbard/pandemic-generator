package gabbard.org.pandemicgenerator

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import gabbard.org.pandemicgenerator.databinding.ActivityRulesetSelectionBinding
import org.gabbard.pandemicgenerator.BUILT_IN_RULE_SETS
import org.gabbard.pandemicgenerator.RuleSet

class RuleSetSelectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRulesetSelectionBinding

    companion object {
        const val RULE_SET = "rule_set"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRulesetSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.createCustomButton.setOnClickListener {
            startActivity(Intent(this, EditRuleSetActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        populateList()
    }

    private fun populateList() {
        val container = binding.ruleSetsContainer
        container.removeAllViews()

        val customRuleSets = RuleSetRepository.load(this)

        for (ruleSet in BUILT_IN_RULE_SETS) {
            addRuleSetRow(container, ruleSet, isBuiltIn = true, customIndex = -1, customList = customRuleSets)
        }
        for ((index, ruleSet) in customRuleSets.withIndex()) {
            addRuleSetRow(container, ruleSet, isBuiltIn = false, customIndex = index, customList = customRuleSets)
        }
    }

    private fun addRuleSetRow(
        container: LinearLayout,
        ruleSet: RuleSet,
        isBuiltIn: Boolean,
        customIndex: Int,
        customList: List<RuleSet>
    ) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = 8.dp }
            setPadding(0, 8.dp, 0, 8.dp)
        }

        val nameView = TextView(this).apply {
            text = ruleSet.name + if (isBuiltIn) " (built-in)" else ""
            textSize = 16f
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        row.addView(nameView)

        val selectButton = Button(this).apply {
            text = "Select"
            setOnClickListener { launchGameOptions(ruleSet) }
        }
        row.addView(selectButton)

        if (!isBuiltIn) {
            val editButton = Button(this).apply {
                text = "Edit"
                setOnClickListener {
                    startActivity(Intent(this@RuleSetSelectionActivity, EditRuleSetActivity::class.java).apply {
                        putExtra(EditRuleSetActivity.RULE_SET, ruleSet)
                        putExtra(EditRuleSetActivity.CUSTOM_INDEX, customIndex)
                    })
                }
            }
            row.addView(editButton)

            val deleteButton = Button(this).apply {
                text = "Delete"
                setOnClickListener {
                    AlertDialog.Builder(this@RuleSetSelectionActivity)
                        .setTitle("Delete rule set")
                        .setMessage("Delete \"${ruleSet.name}\"?")
                        .setPositiveButton("Delete") { _, _ ->
                            val updated = customList.toMutableList().also { it.removeAt(customIndex) }
                            RuleSetRepository.save(this@RuleSetSelectionActivity, updated)
                            populateList()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }
            row.addView(deleteButton)
        }

        container.addView(row)
    }

    private fun launchGameOptions(ruleSet: RuleSet) {
        startActivity(Intent(this, GameOptionsActivity::class.java).apply {
            putExtra(RULE_SET, ruleSet)
        })
    }

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
}
