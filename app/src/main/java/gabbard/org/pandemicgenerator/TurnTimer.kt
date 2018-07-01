package gabbard.org.pandemicgenerator

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_turn_timer.*
import org.gabbard.pandemicgenerator.TrackableState
import java.util.*

class TurnTimer : AppCompatActivity() {
    private var gameState: TrackableState? = null
    private var rng: Random? = null

    companion object {
        const val GAME_STATE = "game_state"
        const val RANDOM_SOURCE = "random_source"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_turn_timer)
        timeRemaining.text = "1:15"
        gameState = intent.getSerializableExtra(GAME_STATE) as TrackableState
        rng = intent.getSerializableExtra(RANDOM_SOURCE) as Random
    }

    fun onDrawPlayerCards(view: View) {
        val drawPlayerCardsIntent = Intent(this, DrawPlayerCards::class.java)
        drawPlayerCardsIntent.putExtra(DrawPlayerCards.GAME_STATE, gameState!!)
        drawPlayerCardsIntent.putExtra(DrawPlayerCards.RANDOM_SOURCE, rng!!)

        startActivity(drawPlayerCardsIntent)
    }
}
