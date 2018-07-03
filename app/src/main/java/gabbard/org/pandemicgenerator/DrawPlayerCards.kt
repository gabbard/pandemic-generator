package gabbard.org.pandemicgenerator

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_draw_player_cards.*
import org.gabbard.pandemicgenerator.TrackableState
import org.gabbard.pandemicgenerator.Transition
import org.gabbard.pandemicgenerator.messageForTransitionResult
import java.util.*

class DrawPlayerCards : AppCompatActivity() {
    private var gameState: TrackableState? = null
    private var rng: Random? = null

    companion object {
        const val GAME_STATE = "game_state"
        const val RANDOM_SOURCE = "random_source"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_draw_player_cards)
        gameState = intent.getSerializableExtra(DrawPlayerCards.GAME_STATE) as TrackableState
        rng = intent.getSerializableExtra(DrawPlayerCards.RANDOM_SOURCE) as Random
        val drawResult = gameState!!.executeTransition(Transition.DRAW_PLAYER_CARDS, rng!!)

        gameState = drawResult.newGameState
        drawResultMessage.text = messageForTransitionResult(drawResult)
    }

    fun onProceedToInfectionPhase(view: View) {
        val infetionIntent = Intent(this, InfectionActivity::class.java)
        infetionIntent.putExtra(InfectionActivity.GAME_STATE, gameState!!)
        infetionIntent.putExtra(InfectionActivity.RANDOM_SOURCE, rng!!)

        startActivity(infetionIntent)
    }
}
