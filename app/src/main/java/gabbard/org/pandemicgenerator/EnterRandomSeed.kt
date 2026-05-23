package gabbard.org.pandemicgenerator

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import gabbard.org.pandemicgenerator.databinding.ActivityEnterRandomSeedBinding
import java.util.*

class EnterRandomSeed : AppCompatActivity() {
    private lateinit var binding: ActivityEnterRandomSeedBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEnterRandomSeedBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    fun startGame(view: View) {
        val rng = Random(binding.startGame.text.toString().toLong())

        val startGameIntent = Intent(this, InitialSetup::class.java)
        startGameIntent.putExtra(InitialSetup.RANDOM_SOURCE, rng)
        startActivity(startGameIntent)
    }
}
