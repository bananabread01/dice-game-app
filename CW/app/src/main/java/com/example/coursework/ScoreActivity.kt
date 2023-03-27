package com.example.coursework

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText

class ScoreActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.score)

        var meWins = 0
        var compWins = 0

        //get information from MainActivity
        val extras = intent.extras
        val meWon = extras?.getString("humanwin")
        if (meWon != null && meWon.isNotEmpty()) {
            meWins = meWon.toInt()
        }

        val compWon = extras?.getString("compwin")
        if (compWon != null && compWon.isNotEmpty()) {
            compWins = compWon.toInt()
        }


        val startBtn: Button = findViewById(R.id.startBtn)
        val toggle: ToggleButton = findViewById(R.id.toggleBtn)
        var mode = 0

        toggle.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                //difficulty mode
                mode = 1
            } else {
                //easy mode
                mode = 0
            }
        }

        //when start button is clicked
        startBtn.setOnClickListener(){
            val targetPoints: AppCompatEditText = findViewById(R.id.inputText)
            val intent = Intent(this, GameActivity::class.java)
            //send information to GameActivity
            intent.putExtra("target", targetPoints.text.toString())
            intent.putExtra("cmode", mode.toString())
            intent.putExtra("humanwin", meWins.toString())
            intent.putExtra("compwin", compWins.toString())
            startActivity(intent)
            finish()
        }
    }
}