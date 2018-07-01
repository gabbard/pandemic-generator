package gabbard.org.pandemicgenerator

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_initial_setup.*
import org.gabbard.pandemicgenerator.GameState


class InitialSetup : AppCompatActivity() {
    companion object {
        const val INITIAL_GAME_STATE = "initial_game_state"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initial_setup)
        val initialGameState = intent.getSerializableExtra(INITIAL_GAME_STATE) as GameState
        val players = initialGameState.trackableState.players
        if (players.size != 2) {
            throw RuntimeException("Only two players currently supported. Issue #10")
        }
        val hands = initialGameState.untrackableState.hands
        player1.text = players[0].role.name + ": " +
                hands[players[0]]
        player2.text = players[1].role.name + ": " +
                hands[players[1]]
        board.text = initialGameState.untrackableState.board.cityStates.toString()
    }

    fun readyToPlay(view: View) {
        val gamePlayIntent = Intent(this, GamePlay::class.java)
        startActivity(gamePlayIntent)
    }
}
