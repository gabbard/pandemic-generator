package gabbard.org.pandemicgenerator

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import gabbard.org.pandemicgenerator.databinding.ActivityEnterRandomSeedBinding
import org.gabbard.pandemicgenerator.GameRules
import java.util.*
import kotlin.math.abs

class EnterRandomSeed : AppCompatActivity() {
    private lateinit var binding: ActivityEnterRandomSeedBinding
    private lateinit var gameRules: GameRules

    companion object {
        const val GAME_RULES = "game_rules"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnterRandomSeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        @Suppress("DEPRECATION")
        gameRules = intent.getSerializableExtra(GAME_RULES) as GameRules

        binding.randomSeedButton.setOnClickListener {
            val seed = abs(Random().nextLong()) % 1_000_000L
            binding.startGame.setText(seed.toString())
        }

        binding.recentSeedsButton.setOnClickListener {
            showRecentSeedsDialog()
        }
    }

    private fun showRecentSeedsDialog() {
        val recentSeeds = SeedHistory.recentSeeds(this)
        if (recentSeeds.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Recent Seeds")
                .setMessage("No recent seeds yet.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        val items = recentSeeds.map { it.toString() }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle("Recent Seeds")
            .setItems(items) { _, which ->
                binding.startGame.setText(items[which])
            }
            .show()
    }

    fun startGame(@Suppress("UNUSED_PARAMETER") view: View) {
        val seed = binding.startGame.text.toString().toLong()
        SeedHistory.record(this, seed)
        startActivity(Intent(this, InitialSetup::class.java).apply {
            putExtra(InitialSetup.GAME_RULES, gameRules)
            putExtra(InitialSetup.RANDOM_SOURCE, Random(seed))
            putExtra(InitialSetup.SEED, seed)
        })
    }
}
