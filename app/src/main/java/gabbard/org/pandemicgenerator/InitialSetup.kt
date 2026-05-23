package gabbard.org.pandemicgenerator

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import gabbard.org.pandemicgenerator.databinding.ActivityInitialSetupBinding
import org.gabbard.pandemicgenerator.NATIONAL_CHAMPIONSHIP_RULES
import org.gabbard.pandemicgenerator.TrackableState
import java.util.*


class InitialSetup : AppCompatActivity() {
    private lateinit var binding: ActivityInitialSetupBinding
    private var gameState: TrackableState? = null
    private var rng: Random? = null

    companion object {
        const val RANDOM_SOURCE = "random_source"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInitialSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        @Suppress("DEPRECATION")
        rng = intent.getSerializableExtra(RANDOM_SOURCE) as Random

        val fullGameState = NATIONAL_CHAMPIONSHIP_RULES.setupGame(rng!!)
        gameState = fullGameState.trackableState

        val players = fullGameState.trackableState.players
        if (players.size != 2) {
            throw RuntimeException("Only two players currently supported. Issue #10")
        }
        val hands = fullGameState.untrackableState.hands
        binding.player1.text = players[0].role.name + ": " + hands[players[0]]
        binding.player2.text = players[1].role.name + ": " + hands[players[1]]
        binding.board.text = fullGameState.untrackableState.board.cityStates.toString()
    }

    fun readyToPlay(view: View) {
        val turnTimerIntent = Intent(this, TurnTimer::class.java)
        turnTimerIntent.putExtra(TurnTimer.GAME_STATE, gameState)
        turnTimerIntent.putExtra(TurnTimer.RANDOM_SOURCE, rng)
        startActivity(turnTimerIntent)
    }
}
