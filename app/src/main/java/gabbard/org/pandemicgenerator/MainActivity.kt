package gabbard.org.pandemicgenerator

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import gabbard.org.pandemicgenerator.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.newGame.setOnClickListener {
            startActivity(Intent(this, EnterRandomSeed::class.java))
        }

        binding.resumeGame.setOnClickListener {
            val session = GameRepository.load(this) ?: return@setOnClickListener
            startActivity(Intent(this, TurnTimer::class.java).apply {
                putExtra(TurnTimer.GAME_STATE, session.trackableState)
                putExtra(TurnTimer.RANDOM_SOURCE, session.rng)
                putExtra(TurnTimer.SEED, session.seed)
            })
        }
    }

    override fun onResume() {
        super.onResume()
        binding.resumeGame.visibility =
            if (GameRepository.exists(this)) View.VISIBLE else View.GONE
    }
}
