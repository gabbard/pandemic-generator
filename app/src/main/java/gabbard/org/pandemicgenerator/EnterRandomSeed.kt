package gabbard.org.pandemicgenerator

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_enter_random_seed.*
import org.gabbard.pandemicgenerator.NATIONAL_CHAMPIONSHIP_RULES
import java.util.*

class EnterRandomSeed : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_random_seed)
    }

    fun startGame(view: View) {
        val rng = Random(startGame.text.toString().toLong())
        val initialSetup = NATIONAL_CHAMPIONSHIP_RULES.setupGame(rng)

        val startGameIntent = Intent(this, InitialSetup::class.java)
        startGameIntent.putExtra(InitialSetup.INITIAL_GAME_STATE, initialSetup)
        startActivity(startGameIntent)
    }
}
