package gabbard.org.pandemicgenerator

import android.app.AlertDialog
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import org.gabbard.pandemicgenerator.TrackableState

abstract class GameActivity : AppCompatActivity() {

    protected open fun gameStateForLog(): TrackableState? = null
    protected open fun seedForLog(): Long = 0

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_game, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_view_log -> {
            val state = gameStateForLog()
            if (state != null) {
                startActivity(Intent(this, GameLogActivity::class.java).apply {
                    putExtra(GameLogActivity.GAME_STATE, state)
                    putExtra(GameLogActivity.SEED, seedForLog())
                })
            }
            true
        }
        R.id.action_end_game -> {
            AlertDialog.Builder(this)
                .setTitle("End Game")
                .setMessage("Are you sure you want to abandon this game?")
                .setPositiveButton("End Game") { _, _ ->
                    GameRepository.clear(this)
                    startActivity(Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                }
                .setNegativeButton("Cancel", null)
                .show()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
