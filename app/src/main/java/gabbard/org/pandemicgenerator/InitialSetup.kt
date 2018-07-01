package gabbard.org.pandemicgenerator

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_initial_setup.*
import org.gabbard.pandemicgenerator.NATIONAL_CHAMPIONSHIP_RULES
import org.gabbard.pandemicgenerator.TrackableState
import java.util.*


class InitialSetup : AppCompatActivity() {
    private var gameState: TrackableState? = null
    private var rng: Random? = null

    companion object {
        const val RANDOM_SOURCE = "random_source"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initial_setup)
        rng = intent.getSerializableExtra(RANDOM_SOURCE) as Random

        val fullGameState = NATIONAL_CHAMPIONSHIP_RULES.setupGame(rng!!)
        gameState = fullGameState.trackableState

        val players = fullGameState.trackableState.players
        if (players.size != 2) {
            throw RuntimeException("Only two players currently supported. Issue #10")
        }
        val hands = fullGameState.untrackableState.hands
        player1.text = players[0].role.name + ": " +
                hands[players[0]]
        player2.text = players[1].role.name + ": " +
                hands[players[1]]
        board.text = fullGameState.untrackableState.board.cityStates.toString()
    }

    fun readyToPlay(view: View) {
        val turnTimerIntent = Intent(this, TurnTimer::class.java)
        turnTimerIntent.putExtra(TurnTimer.GAME_STATE, gameState)
        turnTimerIntent.putExtra(TurnTimer.RANDOM_SOURCE, rng)
        startActivity(turnTimerIntent)
    }
}
