package gabbard.org.pandemicgenerator

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View

class EnterRandomSeed : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_enter_random_seed)
    }

    fun startGame(view: View) {
        val startGameIntent = Intent(this, InitialSetup::class.java)
        startActivity(startGameIntent)
    }
}
