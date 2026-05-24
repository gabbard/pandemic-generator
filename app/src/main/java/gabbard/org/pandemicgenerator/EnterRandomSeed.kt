package gabbard.org.pandemicgenerator

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
    }

    fun startGame(@Suppress("UNUSED_PARAMETER") view: View) {
        val seed = binding.startGame.text.toString().toLong()
        startActivity(Intent(this, InitialSetup::class.java).apply {
            putExtra(InitialSetup.GAME_RULES, gameRules)
            putExtra(InitialSetup.RANDOM_SOURCE, Random(seed))
            putExtra(InitialSetup.SEED, seed)
        })
    }
}
