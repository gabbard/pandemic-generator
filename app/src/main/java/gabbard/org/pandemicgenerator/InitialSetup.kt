package gabbard.org.pandemicgenerator

import android.content.Intent
import android.os.Bundle
import android.view.View
import gabbard.org.pandemicgenerator.databinding.ActivityInitialSetupBinding
import org.gabbard.pandemicgenerator.GameRules
import org.gabbard.pandemicgenerator.GameState
import org.gabbard.pandemicgenerator.TrackableState
import java.util.*


class InitialSetup : GameActivity() {
    private lateinit var binding: ActivityInitialSetupBinding
    private var gameRules: GameRules? = null
    private var rng: Random? = null
    private var seed: Long = 0
    private var gameState: TrackableState? = null
    private var fullGameState: GameState? = null

    companion object {
        const val GAME_RULES = "game_rules"
        const val RANDOM_SOURCE = "random_source"
        const val SEED = "seed"
        private const val FULL_GAME_STATE = "full_game_state"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInitialSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        @Suppress("DEPRECATION")
        gameRules = intent.getSerializableExtra(GAME_RULES) as GameRules
        @Suppress("DEPRECATION")
        rng = intent.getSerializableExtra(RANDOM_SOURCE) as Random
        seed = intent.getLongExtra(SEED, 0)
        binding.seedDisplay.text = "Seed: $seed"

        // setupGame() mutates rng and must only run once per seed; on recreation
        // (rotation, or process death while the activity is still in the back
        // stack) restore the previously computed result instead of redrawing it.
        @Suppress("DEPRECATION")
        fullGameState = if (savedInstanceState == null) {
            gameRules!!.setupGame(rng!!)
        } else {
            savedInstanceState.getSerializable(FULL_GAME_STATE) as GameState
        }
        val trackableState = fullGameState!!.trackableState

        val players = trackableState.players
        val hands = fullGameState!!.untrackableState.hands
        listOf(binding.player1Cards to players[0]).plus(
            if (players.size > 1) listOf(binding.player2Cards to players[1]) else emptyList()
        ).forEach { (container, player) ->
            container.addSectionHeader(player.role.name)
            hands[player]!!.sortedBy { it.userString }.forEach { container.addPlayerCardRow(it) }
        }

        // hide player2 section if only 1 player (shouldn't happen but guard anyway)
        if (players.size < 2) binding.player2Cards.removeAllViews()

        val boardCities = binding.boardCities
        val cityStates = fullGameState!!.untrackableState.board.cityStates
        for (cubes in 3 downTo 1) {
            val cities = cityStates.filterValues { it.infections.values.sum() == cubes }.keys
            if (cities.isNotEmpty()) {
                boardCities.addSectionHeader("$cubes cube${if (cubes == 1) "" else "s"}:")
                cities.sortedBy { it.name }.forEach { boardCities.addCityRow(it) }
            }
        }

        // store trackable state for TurnTimer
        this.gameState = trackableState
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(FULL_GAME_STATE, fullGameState)
    }

    fun readyToPlay(@Suppress("UNUSED_PARAMETER") view: View) {
        startActivity(Intent(this, TurnTimer::class.java).apply {
            putExtra(TurnTimer.GAME_STATE, gameState)
            putExtra(TurnTimer.RANDOM_SOURCE, rng)
            putExtra(TurnTimer.SEED, seed)
            putExtra(TurnTimer.TURN_DURATION, gameRules!!.turnDurationSeconds ?: -1)
            putExtra(TurnTimer.INITIAL_STATE, fullGameState!!.untrackableState)
        })
    }
}
