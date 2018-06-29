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
        player1.text = initialGameState.trackableState.players[0].role.name
    }

    fun readyToPlay(view: View) {
        val gamePlayIntent = Intent(this, GamePlay::class.java)
        startActivity(gamePlayIntent)
    }
}
