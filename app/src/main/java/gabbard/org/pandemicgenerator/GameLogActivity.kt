package gabbard.org.pandemicgenerator

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import gabbard.org.pandemicgenerator.databinding.ActivityGameLogBinding
import org.gabbard.pandemicgenerator.GameEvent
import org.gabbard.pandemicgenerator.TrackableState

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
            log.forEachIndexed { index, event ->
                when (event) {
                    is GameEvent.DrawPlayerCardsEvent -> {
                        container.addSectionHeader("Event ${index + 1}: Drew Player Cards")
                        event.cardsDrawn.forEach { container.addPlayerCardRow(it) }
                        for ((epidemic, city) in event.epidemicsAndInfectedCities) {
                            container.addSectionHeader("Epidemic: ${epidemic.userString}")
                            container.addCityRow(city, "infected from bottom")
                        }
                    }
                    is GameEvent.InfectionEvent -> {
                        container.addSectionHeader("Event ${index + 1}: Infection")
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
