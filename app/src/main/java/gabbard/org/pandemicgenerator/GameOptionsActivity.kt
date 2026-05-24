package gabbard.org.pandemicgenerator

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import gabbard.org.pandemicgenerator.databinding.ActivityGameOptionsBinding
import org.gabbard.pandemicgenerator.Difficulty
import org.gabbard.pandemicgenerator.GameOptions
import org.gabbard.pandemicgenerator.RuleSet

class GameOptionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGameOptionsBinding
    private lateinit var ruleSet: RuleSet

    companion object {
        const val PREFS_NAME = "game_options"
        const val PREF_TURN_DURATION = "turn_duration_seconds"
        const val DEFAULT_TURN_DURATION = 75
        val TURN_DURATIONS = listOf(60, 75, 90, 120)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        @Suppress("DEPRECATION")
        ruleSet = intent.getSerializableExtra(RuleSetSelectionActivity.RULE_SET) as RuleSet
        binding.rulesetName.text = ruleSet.name

        val playerCounts = ruleSet.allowedPlayerCounts
        binding.playerCountSpinner.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item,
            playerCounts.map { "$it players" }
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        val difficulties = ruleSet.availableDifficulties
        binding.difficultySpinner.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item,
            difficulties.map { it.name }
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }

        binding.turnDurationSpinner.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_item,
            TURN_DURATIONS.map { "$it seconds" }
        ).also { it.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        val savedDuration = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
            .getInt(PREF_TURN_DURATION, DEFAULT_TURN_DURATION)
        binding.turnDurationSpinner.setSelection(TURN_DURATIONS.indexOf(savedDuration).coerceAtLeast(0))

        binding.startGameButton.setOnClickListener {
            val numPlayers = playerCounts[binding.playerCountSpinner.selectedItemPosition]
            val difficulty: Difficulty = difficulties[binding.difficultySpinner.selectedItemPosition]
            val turnDuration = TURN_DURATIONS[binding.turnDurationSpinner.selectedItemPosition]
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit()
                .putInt(PREF_TURN_DURATION, turnDuration).apply()
            val gameRules = ruleSet.buildGameRules(GameOptions(numPlayers, difficulty))
            startActivity(Intent(this, EnterRandomSeed::class.java).apply {
                putExtra(EnterRandomSeed.GAME_RULES, gameRules)
            })
        }
    }
}
