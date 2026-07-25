package gabbard.org.pandemicgenerator

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import gabbard.org.pandemicgenerator.databinding.ActivityGameLogBinding
import org.gabbard.pandemicgenerator.GameEvent
import org.gabbard.pandemicgenerator.Player
import org.gabbard.pandemicgenerator.TrackableState
import org.gabbard.pandemicgenerator.seededColorTiebreakOrder

class GameLogActivity : AppCompatActivity() {
    private lateinit var binding: ActivityGameLogBinding

    companion object {
        const val GAME_STATE = "game_state"
        const val SEED = "seed"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameLogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        @Suppress("DEPRECATION")
        val gameState = intent.getSerializableExtra(GAME_STATE) as TrackableState
        val seed = intent.getLongExtra(SEED, 0)
        binding.seedDisplay.text = "Seed: $seed"

        val container = binding.eventLogContainer
        val log = gameState.eventLog
        if (log.isEmpty()) {
            container.addSectionHeader("No events yet")
        } else {
            // Group the chronological event log into per-turn sections: a turn begins
            // whenever a player draws cards, or whenever the acting player changes (which
            // can only happen at a turn boundary), so a player's draw and the infection
            // that follows it are shown together under a single "Turn N — <Player>" header.
            var turnNumber = 0
            var lastPlayer: Player? = null
            var isFirstEpidemicOfGame = true
            log.forEach { event ->
                if (event is GameEvent.DrawPlayerCardsEvent || event.player != lastPlayer) {
                    turnNumber++
                    container.addSectionHeader("Turn $turnNumber — ${event.player.role.name}")
                }
                lastPlayer = event.player
                when (event) {
                    is GameEvent.DrawPlayerCardsEvent -> {
                        event.cardsDrawn.forEach { container.addPlayerCardRow(it) }
                        for ((epidemic, city) in event.epidemicsAndInfectedCities) {
                            container.addSectionHeader("Epidemic: ${epidemic.userString}")
                            container.addCityRow(city, "infected from bottom")
                            if (isFirstEpidemicOfGame) {
                                container.addFirstEpidemicVirulentStrainExplanation(seed)
                                isFirstEpidemicOfGame = false
                            }
                        }
                    }
                    is GameEvent.InfectionEvent -> {
                        event.infectedCities.forEach { container.addCityRow(it) }
                    }
                }
            }
        }
    }

    fun onClose(@Suppress("UNUSED_PARAMETER") view: View) {
        finish()
    }
}
