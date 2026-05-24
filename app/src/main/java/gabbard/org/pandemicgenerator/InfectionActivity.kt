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
    var gameState: TrackableState? = null
    var rng: Random? = null

    companion object {
        const val GAME_STATE = "game_state"
        const val RANDOM_SOURCE = "random_source"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        @Suppress("DEPRECATION")
        gameState = intent.getSerializableExtra(InfectionActivity.GAME_STATE) as TrackableState
        @Suppress("DEPRECATION")
        rng = intent.getSerializableExtra(InfectionActivity.RANDOM_SOURCE) as Random

        val infectionResult = gameState!!.executeTransition(Transition.INFECT, rng!!)
        binding.infectionResultMessage.text = messageForTransitionResult(infectionResult)
        gameState = infectionResult.newGameState
    }

    fun onNextTurn(@Suppress("UNUSED_PARAMETER") view: View) {
        val turnTimerIntent = Intent(this, TurnTimer::class.java)
        turnTimerIntent.putExtra(TurnTimer.GAME_STATE, gameState!!)
        turnTimerIntent.putExtra(TurnTimer.RANDOM_SOURCE, rng!!)
        startActivity(turnTimerIntent)
    }
}
