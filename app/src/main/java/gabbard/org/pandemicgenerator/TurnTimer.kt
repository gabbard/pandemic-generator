package gabbard.org.pandemicgenerator

import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Bundle
import android.os.SystemClock
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Chronometer
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
        gameState = intent.getSerializableExtra(GAME_STATE) as TrackableState
        rng = intent.getSerializableExtra(RANDOM_SOURCE) as Random
        timeRemaining.isCountDown = true
        timeRemaining.start()
        val targetTime = SystemClock.elapsedRealtime() + 75 * 1000
        timeRemaining.base = targetTime
        timeRemaining.onChronometerTickListener = Chronometer.OnChronometerTickListener {
            val timeTilTarget = targetTime - SystemClock.elapsedRealtime()
            if (timeTilTarget <= 0) {
                try {
                    val notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    val r = RingtoneManager.getRingtone(applicationContext, notification)
                    r.play()
                    timeRemaining.stop()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else if (timeTilTarget <= 10 * 1000) {
                window.decorView.setBackgroundColor(Color.RED)
            } else if (timeTilTarget <= 30 * 1000) {
                window.decorView.setBackgroundColor(Color.YELLOW)
            }
        }
    }

    fun onDrawPlayerCards(view: View) {
        val drawPlayerCardsIntent = Intent(this, DrawPlayerCards::class.java)
        drawPlayerCardsIntent.putExtra(DrawPlayerCards.GAME_STATE, gameState!!)
        drawPlayerCardsIntent.putExtra(DrawPlayerCards.RANDOM_SOURCE, rng!!)

        startActivity(drawPlayerCardsIntent)
    }
}
