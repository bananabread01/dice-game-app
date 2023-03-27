package com.example.coursework

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val newGameBtn: Button = findViewById(R.id.gameBtn)
        val aboutBtn: Button = findViewById(R.id.aboutBtn)

        var meWins = 0
        var compWins = 0

        //get information from GameActivity
        val extras = intent.extras
        val meWon = extras?.getString("humanwin")
        if (meWon != null && meWon.isNotEmpty()) {
            meWins = meWon.toInt()
        }

        val compWon = extras?.getString("compwin")
        if (compWon != null && compWon.isNotEmpty()) {
            compWins = compWon.toInt()
        }

        //when new game button is clicked
        newGameBtn.setOnClickListener() {
            val intent = Intent(this, ScoreActivity::class.java)
            //send information to ScoreActivity
            intent.putExtra("humanwin", meWins.toString())
            intent.putExtra("compwin", compWins.toString())
            startActivity(intent)
            finish()
        }

        //when about button is clicked. open about pop up window
        aboutBtn.setOnClickListener{
            val dialogBinder = layoutInflater.inflate(R.layout.about_popup, null)

            val myDialog = Dialog(this)
            myDialog.setContentView(dialogBinder)
            myDialog.setCancelable(true)
            myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            myDialog.show()

            val okBtn: Button = dialogBinder.findViewById(R.id.okBtn)
            //close the pop up when the ok button is clicked
            okBtn.setOnClickListener {
                myDialog.dismiss()
            }
        }
    }
}