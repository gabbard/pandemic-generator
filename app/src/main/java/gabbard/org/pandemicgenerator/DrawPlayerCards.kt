package gabbard.org.pandemicgenerator

import android.content.Intent
import android.os.Bundle
import android.view.View
import gabbard.org.pandemicgenerator.databinding.ActivityDrawPlayerCardsBinding
import org.gabbard.pandemicgenerator.TrackableState
import org.gabbard.pandemicgenerator.Transition
import java.util.*

class DrawPlayerCards : GameActivity() {
    private lateinit var binding: ActivityDrawPlayerCardsBinding
    private var gameState: TrackableState? = null
    private var rng: Random? = null
    private var seed: Long = 0

    companion object {
        const val GAME_STATE = "game_state"
        const val RANDOM_SOURCE = "random_source"
        const val SEED = "seed"
        const val TURN_DURATION = "turn_duration"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDrawPlayerCardsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        @Suppress("DEPRECATION")
        gameState = intent.getSerializableExtra(DrawPlayerCards.GAME_STATE) as TrackableState
        @Suppress("DEPRECATION")
        rng = intent.getSerializableExtra(DrawPlayerCards.RANDOM_SOURCE) as Random
        seed = intent.getLongExtra(SEED, 0)
        binding.seedDisplay.text = "Seed: $seed"

        when (val result = gameState!!.executeTransition(Transition.DRAW_PLAYER_CARDS, rng!!)) {
            is TrackableState.TransitionResult.PlayerDeckExhausted -> {
                startActivity(Intent(this, GameOverActivity::class.java))
                return
            }
            is TrackableState.TransitionResult.Success.DrawPlayerCardsTransitionResult -> {
                gameState = result.newGameState

                val container = binding.cardsContainer
                container.addSectionHeader("Cards drawn:")
                result.cardsDrawn.forEach { container.addPlayerCardRow(it) }

                for ((epidemic, city) in result.epidemicsAndInfectedCities) {
                    container.addSectionHeader("Epidemic: ${epidemic.userString}")
                    container.addCityRow(city, "infected from bottom")
                }
            }
            else -> error("Unexpected result type for DRAW_PLAYER_CARDS: $result")
        }
    }

    fun onProceedToInfectionPhase(@Suppress("UNUSED_PARAMETER") view: View) {
        val infectionIntent = Intent(this, InfectionActivity::class.java)
        infectionIntent.putExtra(InfectionActivity.GAME_STATE, gameState!!)
        infectionIntent.putExtra(InfectionActivity.RANDOM_SOURCE, rng!!)
        infectionIntent.putExtra(InfectionActivity.SEED, seed)
        infectionIntent.putExtra(InfectionActivity.TURN_DURATION, intent.getIntExtra(TURN_DURATION, TurnTimer.NO_TIMER))
        startActivity(infectionIntent)
    }
}
