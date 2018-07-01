package gabbard.org.pandemicgenerator

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_infection.*
import org.gabbard.pandemicgenerator.TrackableState
import org.gabbard.pandemicgenerator.Transition
import java.util.*

class InfectionActivity : AppCompatActivity() {
    var gameState: TrackableState? = null
    var rng: Random? = null

    companion object {
        const val GAME_STATE = "game_state"
        const val RANDOM_SOURCE = "random_source"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_infection)
        gameState = intent.getSerializableExtra(InfectionActivity.GAME_STATE) as TrackableState
        rng = intent.getSerializableExtra(InfectionActivity.RANDOM_SOURCE) as Random

        val infectionResult = gameState!!.executeTransition(Transition.INFECT, rng!!)
        infectionResultMessage.text = infectionResult.message
        gameState = infectionResult.newGameState
    }

    fun onNextTurn(view: View) {
        val turnTimerIntent = Intent(this, TurnTimer::class.java)
        turnTimerIntent.putExtra(TurnTimer.GAME_STATE, gameState!!)
        turnTimerIntent.putExtra(TurnTimer.RANDOM_SOURCE, rng!!)

        startActivity(turnTimerIntent)
    }
}
