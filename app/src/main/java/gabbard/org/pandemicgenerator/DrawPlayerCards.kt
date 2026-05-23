package gabbard.org.pandemicgenerator

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import gabbard.org.pandemicgenerator.databinding.ActivityDrawPlayerCardsBinding
import org.gabbard.pandemicgenerator.TrackableState
import org.gabbard.pandemicgenerator.Transition
import org.gabbard.pandemicgenerator.messageForTransitionResult
import java.util.*

class DrawPlayerCards : AppCompatActivity() {
    private lateinit var binding: ActivityDrawPlayerCardsBinding
    private var gameState: TrackableState? = null
    private var rng: Random? = null

    companion object {
        const val GAME_STATE = "game_state"
        const val RANDOM_SOURCE = "random_source"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDrawPlayerCardsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        gameState = intent.getSerializableExtra(DrawPlayerCards.GAME_STATE) as TrackableState
        rng = intent.getSerializableExtra(DrawPlayerCards.RANDOM_SOURCE) as Random
        val drawResult = gameState!!.executeTransition(Transition.DRAW_PLAYER_CARDS, rng!!)

        gameState = drawResult.newGameState
        binding.drawResultMessage.text = messageForTransitionResult(drawResult)
    }

    fun onProceedToInfectionPhase(view: View) {
        val infetionIntent = Intent(this, InfectionActivity::class.java)
        infetionIntent.putExtra(InfectionActivity.GAME_STATE, gameState!!)
        infetionIntent.putExtra(InfectionActivity.RANDOM_SOURCE, rng!!)
        startActivity(infetionIntent)
    }
}
