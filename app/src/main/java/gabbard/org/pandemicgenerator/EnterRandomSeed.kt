package gabbard.org.pandemicgenerator

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import gabbard.org.pandemicgenerator.databinding.ActivityEnterRandomSeedBinding
import java.util.*
import kotlin.math.abs

class EnterRandomSeed : AppCompatActivity() {
    private lateinit var binding: ActivityEnterRandomSeedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnterRandomSeedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.randomSeedButton.setOnClickListener {
            val seed = abs(Random().nextLong()) % 1_000_000L
            binding.startGame.setText(seed.toString())
        }
    }

    fun startGame(@Suppress("UNUSED_PARAMETER") view: View) {
        val seed = binding.startGame.text.toString().toLong()
        val startGameIntent = Intent(this, InitialSetup::class.java)
        startGameIntent.putExtra(InitialSetup.RANDOM_SOURCE, Random(seed))
        startGameIntent.putExtra(InitialSetup.SEED, seed)
        startActivity(startGameIntent)
    }
}
