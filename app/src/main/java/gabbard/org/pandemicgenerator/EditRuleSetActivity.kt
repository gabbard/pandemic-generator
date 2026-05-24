package gabbard.org.pandemicgenerator

import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import gabbard.org.pandemicgenerator.databinding.ActivityEditRulesetBinding
import org.gabbard.pandemicgenerator.*

class EditRuleSetActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditRulesetBinding

    companion object {
        const val RULE_SET = "rule_set"
        const val CUSTOM_INDEX = "custom_index"
    }

    private val difficultyRows = mutableListOf<DifficultyRow>()

    private data class DifficultyRow(val nameField: EditText, val countField: EditText)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditRulesetBinding.inflate(layoutInflater)
        setContentView(binding.root)

        @Suppress("DEPRECATION")
        val existingRuleSet = intent.getSerializableExtra(RULE_SET) as? RuleSet
        val customIndex = intent.getIntExtra(CUSTOM_INDEX, -1)

        // Populate named epidemic checkboxes (hidden until "Named" radio selected)
        val sortedNamedEpidemics = VIRULENT_STRAIN_EPIDEMICS.sortedBy { it.name }
        for (epidemic in sortedNamedEpidemics) {
            val cb = CheckBox(this).apply { text = epidemic.name }
            binding.namedEpidemicsContainer.addView(cb)
        }
        binding.namedEpidemicsContainer.visibility = android.view.View.GONE
        binding.epidemicTypeGroup.setOnCheckedChangeListener { _, checkedId ->
            binding.namedEpidemicsContainer.visibility =
                if (checkedId == R.id.epidemic_named) android.view.View.VISIBLE else android.view.View.GONE
        }

        // Pre-populate all event checkboxes from ALL_KNOWN_EVENTS
        val sortedEvents = ALL_KNOWN_EVENTS.sortedBy { it.name }
        val eventCheckBoxes = mutableMapOf<EventCard, CheckBox>()
        for (event in sortedEvents) {
            val cb = CheckBox(this).apply { text = event.name }
            binding.eventsContainer.addView(cb)
            eventCheckBoxes[event] = cb
        }

        // Pre-populate role checkboxes from ALL_ROLES
        val sortedRoles = ALL_ROLES.sortedBy { it.name }
        val roleCheckBoxes = mutableMapOf<Role, CheckBox>()
        for (role in sortedRoles) {
            val cb = CheckBox(this).apply { text = role.name }
            binding.rolesContainer.addView(cb)
            roleCheckBoxes[role] = cb
        }

        // Set up "Add Difficulty" button
        binding.addDifficultyButton.setOnClickListener { addDifficultyRow(null) }

        if (existingRuleSet != null) {
            populateFromRuleSet(existingRuleSet, eventCheckBoxes, roleCheckBoxes)
        } else {
            binding.playerCount2.isChecked = true
            binding.playerCount3.isChecked = true
            binding.playerCount4.isChecked = true
            for (difficulty in STANDARD_PANDEMIC.availableDifficulties) {
                addDifficultyRow(difficulty)
            }
        }

        binding.saveButton.setOnClickListener {
            val ruleSet = buildRuleSet(eventCheckBoxes, roleCheckBoxes) ?: return@setOnClickListener
            val customList = RuleSetRepository.load(this).toMutableList()
            if (customIndex >= 0) {
                customList[customIndex] = ruleSet
            } else {
                customList.add(ruleSet)
            }
            RuleSetRepository.save(this, customList)
            finish()
        }
    }

    private fun populateFromRuleSet(
        ruleSet: RuleSet,
        eventCheckBoxes: Map<EventCard, CheckBox>,
        roleCheckBoxes: Map<Role, CheckBox>
    ) {
        binding.rulesetName.setText(ruleSet.name)

        binding.playerCount2.isChecked = 2 in ruleSet.allowedPlayerCounts
        binding.playerCount3.isChecked = 3 in ruleSet.allowedPlayerCounts
        binding.playerCount4.isChecked = 4 in ruleSet.allowedPlayerCounts

        val hasSimpleEpidemics = ruleSet.availableEpidemics.all { it is SimpleEpidemic }
        if (hasSimpleEpidemics) {
            binding.epidemicSimple.isChecked = true
        } else {
            binding.epidemicNamed.isChecked = true
            populateNamedEpidemicCheckboxes(ruleSet.availableEpidemics)
        }

        for (difficulty in ruleSet.availableDifficulties) {
            addDifficultyRow(difficulty)
        }

        for ((event, cb) in eventCheckBoxes) {
            cb.isChecked = event in ruleSet.availableEvents
        }
        binding.numEventsToUse.setText(ruleSet.numEventsToUse.toString())

        for ((role, cb) in roleCheckBoxes) {
            cb.isChecked = role in ruleSet.availableRoles
        }
    }

    private fun populateNamedEpidemicCheckboxes(epidemics: Set<Epidemic>) {
        val namedEpidemics = epidemics.filterIsInstance<NamedEpidemic>().map { it.name }.toSet()
        for (i in 0 until binding.namedEpidemicsContainer.childCount) {
            val cb = binding.namedEpidemicsContainer.getChildAt(i) as? CheckBox ?: continue
            cb.isChecked = cb.text.toString() in namedEpidemics
        }
    }

    private fun addDifficultyRow(difficulty: org.gabbard.pandemicgenerator.Difficulty?) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = 4.dp }
        }

        val nameField = EditText(this).apply {
            hint = "Name"
            setText(difficulty?.name ?: "")
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f)
        }
        row.addView(nameField)

        val countField = EditText(this).apply {
            hint = "#"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setText(difficulty?.numEpidemics?.toString() ?: "")
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        row.addView(countField)

        val deleteButton = Button(this).apply {
            text = "✕"
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                binding.difficultiesContainer.removeView(row)
                difficultyRows.removeAll { it.nameField == nameField }
            }
        }
        row.addView(deleteButton)

        binding.difficultiesContainer.addView(row)
        difficultyRows.add(DifficultyRow(nameField, countField))
    }

    private fun buildRuleSet(
        eventCheckBoxes: Map<EventCard, CheckBox>,
        roleCheckBoxes: Map<Role, CheckBox>
    ): RuleSet? {
        val name = binding.rulesetName.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(this, "Name is required", Toast.LENGTH_SHORT).show()
            return null
        }

        val allowedPlayerCounts = listOfNotNull(
            if (binding.playerCount2.isChecked) 2 else null,
            if (binding.playerCount3.isChecked) 3 else null,
            if (binding.playerCount4.isChecked) 4 else null
        )
        if (allowedPlayerCounts.isEmpty()) {
            Toast.makeText(this, "Select at least one player count", Toast.LENGTH_SHORT).show()
            return null
        }

        val difficulties = difficultyRows.mapNotNull { row ->
            val dName = row.nameField.text.toString().trim()
            val count = row.countField.text.toString().toIntOrNull()
            if (dName.isEmpty() || count == null || count < 1) null
            else org.gabbard.pandemicgenerator.Difficulty(dName, count)
        }
        if (difficulties.isEmpty()) {
            Toast.makeText(this, "Add at least one difficulty", Toast.LENGTH_SHORT).show()
            return null
        }

        val maxEpidemics = difficulties.maxOf { it.numEpidemics }
        val availableEpidemics: Set<Epidemic> = if (binding.epidemicSimple.isChecked) {
            (1..maxEpidemics).map { SimpleEpidemic() }.toSet()
        } else {
            val selected = mutableSetOf<Epidemic>()
            for (i in 0 until binding.namedEpidemicsContainer.childCount) {
                val cb = binding.namedEpidemicsContainer.getChildAt(i) as? CheckBox ?: continue
                if (cb.isChecked) selected.add(NamedEpidemic(cb.text.toString()))
            }
            if (selected.size < maxEpidemics) {
                Toast.makeText(
                    this,
                    "Select at least $maxEpidemics epidemic cards (max difficulty needs $maxEpidemics)",
                    Toast.LENGTH_LONG
                ).show()
                return null
            }
            selected
        }

        val availableEvents = eventCheckBoxes.filter { it.value.isChecked }.keys.toSet()
        val numEventsToUse = binding.numEventsToUse.text.toString().toIntOrNull() ?: 0
        if (numEventsToUse > availableEvents.size) {
            Toast.makeText(
                this,
                "Can't use $numEventsToUse events when only ${availableEvents.size} are selected",
                Toast.LENGTH_LONG
            ).show()
            return null
        }

        val availableRoles = roleCheckBoxes.filter { it.value.isChecked }.keys.toSet()
        val maxPlayers = allowedPlayerCounts.max()
        if (availableRoles.size < maxPlayers) {
            Toast.makeText(
                this,
                "Need at least $maxPlayers roles for $maxPlayers players",
                Toast.LENGTH_SHORT
            ).show()
            return null
        }

        return RuleSet(
            name = name,
            availableRoles = availableRoles,
            availableEpidemics = availableEpidemics,
            availableEvents = availableEvents,
            numEventsToUse = numEventsToUse,
            availableDifficulties = difficulties,
            allowedPlayerCounts = allowedPlayerCounts
        )
    }

    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()
}
