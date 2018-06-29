package gabbard.org.pandemicgenerator

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View

class InitialSetup : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_initial_setup)
    }

    fun readyToPlay(view: View) {
        val gamePlayIntent = Intent(this, GamePlay::class.java)
        startActivity(gamePlayIntent)
    }
}
