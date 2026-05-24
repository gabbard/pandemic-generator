package gabbard.org.pandemicgenerator

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import gabbard.org.pandemicgenerator.databinding.ActivityInfectionBinding
import org.gabbard.pandemicgenerator.TrackableState
import org.gabbard.pandemicgenerator.Transition
import org.gabbard.pandemicgenerator.messageForTransitionResult
import java.util.*

class InfectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInfectionBinding
    private var gameState: TrackableState? = null
    private var rng: Random? = null
    private var seed: Long = 0

    companion object {
        const val GAME_STATE = "game_state"
        const val RANDOM_SOURCE = "random_source"
        const val SEED = "seed"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        @Suppress("DEPRECATION")
        gameState = intent.getSerializableExtra(InfectionActivity.GAME_STATE) as TrackableState
        @Suppress("DEPRECATION")
        rng = intent.getSerializableExtra(InfectionActivity.RANDOM_SOURCE) as Random
        seed = intent.getLongExtra(SEED, 0)
        binding.seedDisplay.text = "Seed: $seed"

        val infectionResult = gameState!!.executeTransition(Transition.INFECT, rng!!)
        binding.infectionResultMessage.text = messageForTransitionResult(infectionResult)
        gameState = infectionResult.newGameState
    }

    fun onNextTurn(@Suppress("UNUSED_PARAMETER") view: View) {
        val turnTimerIntent = Intent(this, TurnTimer::class.java)
        turnTimerIntent.putExtra(TurnTimer.GAME_STATE, gameState!!)
        turnTimerIntent.putExtra(TurnTimer.RANDOM_SOURCE, rng!!)
        turnTimerIntent.putExtra(TurnTimer.SEED, seed)
        startActivity(turnTimerIntent)
    }
}
