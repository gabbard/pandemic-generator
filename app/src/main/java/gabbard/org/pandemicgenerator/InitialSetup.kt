package gabbard.org.pandemicgenerator

import android.content.Intent
import android.os.Bundle
import android.view.View
import gabbard.org.pandemicgenerator.databinding.ActivityInitialSetupBinding
import org.gabbard.pandemicgenerator.NATIONAL_CHAMPIONSHIP_RULES
import org.gabbard.pandemicgenerator.TrackableState
import java.util.*


class InitialSetup : GameActivity() {
    private lateinit var binding: ActivityInitialSetupBinding
    private var gameState: TrackableState? = null
    private var rng: Random? = null
    private var seed: Long = 0

    companion object {
        const val RANDOM_SOURCE = "random_source"
        const val SEED = "seed"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInitialSetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        @Suppress("DEPRECATION")
        rng = intent.getSerializableExtra(RANDOM_SOURCE) as Random
        seed = intent.getLongExtra(SEED, 0)
        binding.seedDisplay.text = "Seed: $seed"

        val fullGameState = NATIONAL_CHAMPIONSHIP_RULES.setupGame(rng!!)
        gameState = fullGameState.trackableState

        val players = fullGameState.trackableState.players
        if (players.size != 2) {
            throw RuntimeException("Only two players currently supported. Issue #10")
        }
        val hands = fullGameState.untrackableState.hands
        listOf(binding.player1Cards to players[0], binding.player2Cards to players[1])
            .forEach { (container, player) ->
                container.addSectionHeader(player.role.name)
                hands[player]!!.sortedBy { it.userString }.forEach { container.addPlayerCardRow(it) }
            }

        val boardCities = binding.boardCities
        val cityStates = fullGameState.untrackableState.board.cityStates
        for (cubes in 3 downTo 1) {
            val cities = cityStates.filterValues { it.infections.values.sum() == cubes }.keys
            if (cities.isNotEmpty()) {
                boardCities.addSectionHeader("$cubes cube${if (cubes == 1) "" else "s"}:")
                cities.sortedBy { it.name }.forEach { boardCities.addCityRow(it) }
            }
        }
    }

    fun readyToPlay(@Suppress("UNUSED_PARAMETER") view: View) {
        val turnTimerIntent = Intent(this, TurnTimer::class.java)
        turnTimerIntent.putExtra(TurnTimer.GAME_STATE, gameState)
        turnTimerIntent.putExtra(TurnTimer.RANDOM_SOURCE, rng)
        turnTimerIntent.putExtra(TurnTimer.SEED, seed)
        startActivity(turnTimerIntent)
    }
}
