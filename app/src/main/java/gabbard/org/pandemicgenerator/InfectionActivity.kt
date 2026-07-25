package gabbard.org.pandemicgenerator

import android.content.Intent
import android.os.Bundle
import android.view.View
import gabbard.org.pandemicgenerator.databinding.ActivityInfectionBinding
import org.gabbard.pandemicgenerator.City
import org.gabbard.pandemicgenerator.TrackableState
import org.gabbard.pandemicgenerator.Transition
import org.gabbard.pandemicgenerator.UntrackableState
import java.util.*

class InfectionActivity : GameActivity() {
    private lateinit var binding: ActivityInfectionBinding
    private var gameState: TrackableState? = null
    private var rng: Random? = null
    private var seed: Long = 0
    private var infectedCities: List<City> = emptyList()
    private var initialState: UntrackableState? = null

    companion object {
        const val GAME_STATE = "game_state"
        const val RANDOM_SOURCE = "random_source"
        const val SEED = "seed"
        const val TURN_DURATION = "turn_duration"
        const val INITIAL_STATE = "initial_state"
        private const val RESULT_GAME_STATE = "result_game_state"
        private const val RESULT_INFECTED_CITIES = "result_infected_cities"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInfectionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        @Suppress("DEPRECATION")
        gameState = intent.getSerializableExtra(InfectionActivity.GAME_STATE) as TrackableState
        @Suppress("DEPRECATION")
        rng = intent.getSerializableExtra(InfectionActivity.RANDOM_SOURCE) as Random
        seed = intent.getLongExtra(SEED, 0)
        @Suppress("DEPRECATION")
        initialState = intent.getSerializableExtra(INITIAL_STATE) as? UntrackableState
        binding.seedDisplay.text = "Seed: $seed"
        binding.currentPlayerRole.text = gameState!!.players[gameState!!.curPlayer].role.name

        // executeTransition() mutates rng and must only run once per turn; on
        // recreation (rotation, or process death while the activity is still in
        // the back stack) restore the previously computed result instead of
        // redrawing it.
        if (savedInstanceState == null) {
            val result = gameState!!.executeTransition(Transition.INFECT, rng!!)
                    as TrackableState.TransitionResult.Success.InfectionTransitionResult
            gameState = result.newGameState
            infectedCities = result.infectedCities
        } else {
            @Suppress("DEPRECATION")
            gameState = savedInstanceState.getSerializable(RESULT_GAME_STATE) as TrackableState
            @Suppress("UNCHECKED_CAST", "DEPRECATION")
            infectedCities = savedInstanceState.getSerializable(RESULT_INFECTED_CITIES) as List<City>
        }

        binding.infectedCities.addSectionHeader("Cities infected this turn:")
        infectedCities.forEach { binding.infectedCities.addCityRow(it) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(RESULT_GAME_STATE, gameState)
        outState.putSerializable(RESULT_INFECTED_CITIES, ArrayList(infectedCities))
    }

    override fun gameStateForLog() = gameState
    override fun seedForLog() = seed
    override fun initialStateForLog() = initialState

    fun onNextTurn(@Suppress("UNUSED_PARAMETER") view: View) {
        val turnTimerIntent = Intent(this, TurnTimer::class.java)
        turnTimerIntent.putExtra(TurnTimer.GAME_STATE, gameState!!)
        turnTimerIntent.putExtra(TurnTimer.RANDOM_SOURCE, rng!!)
        turnTimerIntent.putExtra(TurnTimer.SEED, seed)
        turnTimerIntent.putExtra(TurnTimer.TURN_DURATION, intent.getIntExtra(TURN_DURATION, TurnTimer.NO_TIMER))
        initialState?.let { turnTimerIntent.putExtra(TurnTimer.INITIAL_STATE, it) }
        startActivity(turnTimerIntent)
    }
}
