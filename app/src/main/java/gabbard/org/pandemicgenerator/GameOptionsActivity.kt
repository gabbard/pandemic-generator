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

        binding.startGameButton.setOnClickListener {
            val numPlayers = playerCounts[binding.playerCountSpinner.selectedItemPosition]
            val difficulty: Difficulty = difficulties[binding.difficultySpinner.selectedItemPosition]
            val gameRules = ruleSet.buildGameRules(GameOptions(numPlayers, difficulty))
            startActivity(Intent(this, EnterRandomSeed::class.java).apply {
                putExtra(EnterRandomSeed.GAME_RULES, gameRules)
            })
        }
    }
}
