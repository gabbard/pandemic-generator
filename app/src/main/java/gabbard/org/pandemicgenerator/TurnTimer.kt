package gabbard.org.pandemicgenerator

import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Chronometer
import gabbard.org.pandemicgenerator.databinding.ActivityTurnTimerBinding
import org.gabbard.pandemicgenerator.TrackableState
import java.util.*


class TurnTimer : GameActivity() {
    private lateinit var binding: ActivityTurnTimerBinding
    private var gameState: TrackableState? = null
    private var rng: Random? = null
    private var seed: Long = 0
    private var timerExpired = false

    companion object {
        const val GAME_STATE = "game_state"
        const val RANDOM_SOURCE = "random_source"
        const val SEED = "seed"
        const val TURN_DURATION = "turn_duration"
        const val NO_TIMER = -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTurnTimerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        @Suppress("DEPRECATION")
        gameState = intent.getSerializableExtra(GAME_STATE) as TrackableState
        @Suppress("DEPRECATION")
        rng = intent.getSerializableExtra(RANDOM_SOURCE) as Random
        seed = intent.getLongExtra(SEED, 0)
        binding.seedDisplay.text = "Seed: $seed"
        binding.currentPlayerRole.text = gameState!!.players[gameState!!.curPlayer].role.name

        GameRepository.save(this, GameRepository.GameSession(gameState!!, rng!!, seed))

        val turnDurationSeconds = intent.getIntExtra(TURN_DURATION, NO_TIMER)
        if (turnDurationSeconds == NO_TIMER) {
            binding.timeRemaining.visibility = View.GONE
        } else {
            binding.timeRemaining.isCountDown = true
            binding.timeRemaining.start()
            val targetTime = SystemClock.elapsedRealtime() + turnDurationSeconds * 1000L
            binding.timeRemaining.base = targetTime
            binding.timeRemaining.onChronometerTickListener = Chronometer.OnChronometerTickListener {
                val timeTilTarget = targetTime - SystemClock.elapsedRealtime()
                if (timeTilTarget <= 0) {
                    // Chronometer computes its own displayed text from the current clock
                    // reading as part of every tick, independently of this listener. If we
                    // let that happen, it can render a negative time (e.g. "-0:01") for one
                    // tick before stop() takes effect. Force the text to a clean zero state
                    // immediately so nothing negative is ever visible, then stop the timer.
                    binding.timeRemaining.text = "0:00"
                    binding.timeRemaining.stop()
                    if (!timerExpired) {
                        timerExpired = true
                        try {
                            val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                            val r = RingtoneManager.getRingtone(applicationContext, notification)
                            r.play()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                } else if (timeTilTarget <= 10 * 1000) {
                    window.decorView.setBackgroundColor(Color.RED)
                } else if (timeTilTarget <= 30 * 1000) {
                    window.decorView.setBackgroundColor(Color.YELLOW)
                }
            }
        }
    }

    override fun gameStateForLog() = gameState
    override fun seedForLog() = seed

    fun onDrawPlayerCards(@Suppress("UNUSED_PARAMETER") view: View) {
        val drawPlayerCardsIntent = Intent(this, DrawPlayerCards::class.java)
        drawPlayerCardsIntent.putExtra(DrawPlayerCards.GAME_STATE, gameState!!)
        drawPlayerCardsIntent.putExtra(DrawPlayerCards.RANDOM_SOURCE, rng!!)
        drawPlayerCardsIntent.putExtra(DrawPlayerCards.SEED, seed)
        drawPlayerCardsIntent.putExtra(DrawPlayerCards.TURN_DURATION, intent.getIntExtra(TURN_DURATION, NO_TIMER))
        startActivity(drawPlayerCardsIntent)
    }
}
