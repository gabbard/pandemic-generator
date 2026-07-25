package gabbard.org.pandemicgenerator

import android.content.Intent
import android.os.Bundle
import android.view.View
import gabbard.org.pandemicgenerator.databinding.ActivityDrawPlayerCardsBinding
import org.gabbard.pandemicgenerator.Epidemic
import org.gabbard.pandemicgenerator.City
import org.gabbard.pandemicgenerator.PlayerCard
import org.gabbard.pandemicgenerator.TrackableState
import org.gabbard.pandemicgenerator.Transition
import java.util.*

class DrawPlayerCards : GameActivity() {
    private lateinit var binding: ActivityDrawPlayerCardsBinding
    private var gameState: TrackableState? = null
    private var rng: Random? = null
    private var seed: Long = 0
    private var playerDeckExhausted = false
    private var cardsDrawn: List<PlayerCard> = emptyList()
    private var epidemicsAndInfectedCities: List<Pair<Epidemic, City>> = emptyList()

    companion object {
        const val GAME_STATE = "game_state"
        const val RANDOM_SOURCE = "random_source"
        const val SEED = "seed"
        const val TURN_DURATION = "turn_duration"
        private const val RESULT_PLAYER_DECK_EXHAUSTED = "result_player_deck_exhausted"
        private const val RESULT_GAME_STATE = "result_game_state"
        private const val RESULT_CARDS_DRAWN = "result_cards_drawn"
        private const val RESULT_EPIDEMICS_AND_INFECTED_CITIES = "result_epidemics_and_infected_cities"
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
        binding.currentPlayerRole.text = gameState!!.players[gameState!!.curPlayer].role.name

        // executeTransition() mutates rng and must only run once per turn; on
        // recreation (rotation, or process death while the activity is still in
        // the back stack) restore the previously computed result instead of
        // redrawing it.
        if (savedInstanceState == null) {
            when (val result = gameState!!.executeTransition(Transition.DRAW_PLAYER_CARDS, rng!!)) {
                is TrackableState.TransitionResult.PlayerDeckExhausted -> {
                    playerDeckExhausted = true
                    startActivity(Intent(this, GameOverActivity::class.java))
                    return
                }
                is TrackableState.TransitionResult.Success.DrawPlayerCardsTransitionResult -> {
                    gameState = result.newGameState
                    cardsDrawn = result.cardsDrawn
                    epidemicsAndInfectedCities = result.epidemicsAndInfectedCities
                }
                else -> error("Unexpected result type for DRAW_PLAYER_CARDS: $result")
            }
        } else {
            playerDeckExhausted = savedInstanceState.getBoolean(RESULT_PLAYER_DECK_EXHAUSTED)
            if (playerDeckExhausted) {
                startActivity(Intent(this, GameOverActivity::class.java))
                return
            }
            @Suppress("DEPRECATION")
            gameState = savedInstanceState.getSerializable(RESULT_GAME_STATE) as TrackableState
            @Suppress("UNCHECKED_CAST", "DEPRECATION")
            cardsDrawn = savedInstanceState.getSerializable(RESULT_CARDS_DRAWN) as List<PlayerCard>
            @Suppress("UNCHECKED_CAST", "DEPRECATION")
            epidemicsAndInfectedCities = savedInstanceState.getSerializable(
                RESULT_EPIDEMICS_AND_INFECTED_CITIES
            ) as List<Pair<Epidemic, City>>
        }

        val container = binding.cardsContainer
        container.addSectionHeader("Cards drawn:")
        cardsDrawn.forEach { container.addPlayerCardRow(it) }

        for ((epidemic, city) in epidemicsAndInfectedCities) {
            container.addSectionHeader("Epidemic: ${epidemic.userString}")
            container.addCityRow(city, "infected from bottom")
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(RESULT_PLAYER_DECK_EXHAUSTED, playerDeckExhausted)
        if (!playerDeckExhausted) {
            outState.putSerializable(RESULT_GAME_STATE, gameState)
            outState.putSerializable(RESULT_CARDS_DRAWN, ArrayList(cardsDrawn))
            outState.putSerializable(RESULT_EPIDEMICS_AND_INFECTED_CITIES, ArrayList(epidemicsAndInfectedCities))
        }
    }

    override fun gameStateForLog() = gameState
    override fun seedForLog() = seed

    fun onProceedToInfectionPhase(@Suppress("UNUSED_PARAMETER") view: View) {
        val infectionIntent = Intent(this, InfectionActivity::class.java)
        infectionIntent.putExtra(InfectionActivity.GAME_STATE, gameState!!)
        infectionIntent.putExtra(InfectionActivity.RANDOM_SOURCE, rng!!)
        infectionIntent.putExtra(InfectionActivity.SEED, seed)
        infectionIntent.putExtra(InfectionActivity.TURN_DURATION, intent.getIntExtra(TURN_DURATION, TurnTimer.NO_TIMER))
        startActivity(infectionIntent)
    }
}
