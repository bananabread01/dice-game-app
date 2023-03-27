package com.example.coursework

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.*

class GameActivity  : AppCompatActivity() {

    //holds the 6 dice faces
    val diceImages = arrayOf(
        R.drawable.d1,
        R.drawable.d2,
        R.drawable.d3,
        R.drawable.d4,
        R.drawable.d5,
        R.drawable.d6
    )

    private val random = Random()

    companion object{
        //total player wins
        private var meWins = 0
        private var compWins = 0
    }

    private var rounds = 1
    private var winningScore = 101
    private val numRolls = 3
    private var rollsLeft = numRolls
    private var tieRound = 1

    //player total score
    private var meTotalScore = 0
    private var compTotalScore = 0
    
    //computer strategy mode
    private var compMode = 0

    //holds the selected dices
    private val selectedDices = mutableListOf<ImageButton>()
    private val selectedCompDices = mutableListOf<ImageButton>()

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_play)

        //get information from scoreActivity
        val extras = intent.extras
        if (extras != null) {
            //the new set target points
            val targetPoints = extras.getString("target")
            if (targetPoints != null && targetPoints.isNotEmpty()) {
                winningScore = targetPoints.toInt()
            }

            //difficulty mode for computer strategy
            val mode = extras.getString("cmode")
            if (mode != null && mode.isNotEmpty()) {
                compMode = mode.toInt()
            }

            //number of human wins
            val meWon = extras.getString("humanwin")
            if (meWon != null && meWon.isNotEmpty()) {
                meWins = meWon.toInt()
            }

            //number of computer wins
            val compWon = extras.getString("compwin")
            if (compWon != null && compWon.isNotEmpty()) {
                compWins = compWon.toInt()
            }
        }

        val roundsTextView: TextView = findViewById(R.id.round_tv)
        val scoreTextView: TextView = findViewById(R.id.score_tv)
        val winsTextView: TextView = findViewById(R.id.win_tv)

        val throwBtn: Button = findViewById(R.id.throwBtn)
        val scoreBtn: Button = findViewById(R.id.scoreBtn)

        // Display the dices for human player
        val meDiceBtn: Array<ImageButton> = arrayOf(
            this.findViewById(R.id.m1),
            this.findViewById(R.id.m2),
            this.findViewById(R.id.m3),
            this.findViewById(R.id.m4),
            this.findViewById(R.id.m5)
        )

        // Display the dices for computer
        val compDiceBtn: Array<ImageButton> = arrayOf(
            findViewById(R.id.c1),
            findViewById(R.id.c2),
            findViewById(R.id.c3),
            findViewById(R.id.c4),
            findViewById(R.id.c5)
        )

        //select specific dice for me(human)
        selectDice(meDiceBtn)

        //array holds random dice values
        val meDiceValues = IntArray(5)
        val compDiceValues = IntArray(5)
        val selectedDiceValues = IntArray(5)


        //when throw button is clicked
        throwBtn.setOnClickListener{
            if (selectedDices.isEmpty()){
                //throws player's all 5 dice
                throwDice(meDiceValues, meDiceBtn)
                if(rollsLeft==3){
                    //throws computer's dice only for first roll
                    throwDice(compDiceValues, compDiceBtn)
                }
            } else {
                //https://stackoverflow.com/questions/69323625/how-can-i-filter-out-an-item-by-its-id
                //this list holds only selected/highlighted buttons
                val meSelectedDice = meDiceBtn.filter {
                        ibtn -> selectedDices.none{yitem->yitem.id == ibtn.id}}
                //generate random dice values
                for (i in meSelectedDice.indices) selectedDiceValues[i] = random.nextInt(6) + 1

                //set the corresponding images to values for the selected dice
                for (i in meSelectedDice.indices) {
                    meSelectedDice[i].setImageResource(diceImages[selectedDiceValues[i] - 1])
                    //rotate each selected dice
                    rotateAnimation(meSelectedDice[i])
                }
            }

            rollsLeft--
            if (rollsLeft <= 2) throwBtn.text = "Reroll"

            //when all 3 rolls are used
            if(rollsLeft == 0)  {

                //tally up the score
                meTotalScore += score(meDiceValues) + score(selectedDiceValues)
                compTotalScore += score(compDiceValues)

                Toast.makeText(this, "Next round! Score updated", Toast.LENGTH_SHORT).show()
                //update the score and rounds
                scoreTextView.text = "Score\nH:${meTotalScore} /C:${compTotalScore} "
                rounds++
                roundsTextView.text = "Round ${rounds}"

                //if human wins
                if (meTotalScore >= winningScore && meTotalScore > compTotalScore) {
                    meWins++
                    //win pop up window appears
                    val dialogBinder = layoutInflater.inflate(R.layout.win_popup, null)

                    val myDialog = Dialog(this)
                    myDialog.setContentView(dialogBinder)
                    myDialog.setCancelable(false)
                    myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    myDialog.show()

                    val backBtn = dialogBinder.findViewById<Button>(R.id.backBtn)
                    backBtn.setOnClickListener {
                        //open new activity. goes back to main/home screen
                        var intent = Intent(this, MainActivity::class.java)
                        //send information to mainActivity. send the number of wins
                        intent.putExtra("humanwin", meWins.toString())
                        intent.putExtra("compwin", compWins.toString())
                        startActivity(intent)
                        finish()
                    }

                } else if(compTotalScore >= winningScore) {
                    compWins++
                    val dialogBinder = layoutInflater.inflate(R.layout.lose_popup, null)

                    val myDialog = Dialog(this)
                    myDialog.setContentView(dialogBinder)
                    myDialog.setCancelable(false)
                    myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    myDialog.show()

                    val backBtn = dialogBinder.findViewById<Button>(R.id.backBtn)
                    backBtn.setOnClickListener {
                        var intent = Intent(this, MainActivity::class.java)
                        //send information to mainActivity. send the number of wins
                        intent.putExtra("humanwin", meWins.toString())
                        intent.putExtra("compwin", compWins.toString())
                        startActivity(intent)
                        finish()
                    }
                }

                //remove the human's highlighted buttons
                for (element in meDiceBtn){
                    element.setBackgroundResource(R.drawable.deselect_dice)
                    selectedDices.remove(element)
                }

                //remove the computer's highlighted buttons
                for (element in compDiceBtn){
                    element.setBackgroundResource(R.drawable.deselect_dice)
                    selectedCompDices.remove(element)
                }

                throwBtn.text = "Throw"
                rollsLeft = 3
                //update wins
                winsTextView.text = "Wins\nH:${meWins} /C:${compWins}"
            }
        }


        //when score button is clicked
        scoreBtn.setOnClickListener{

            val diceVal = IntArray(5)
            val reroll = random.nextInt(2)+1

            // easy mode computer random strategy
            if (compMode == 0){

                when(reroll){
                    1 -> computerRandomStrategy(compDiceBtn, diceVal)
                    2 -> {
                        computerRandomStrategy(compDiceBtn, diceVal)
                        computerRandomStrategy(compDiceBtn, diceVal)
                    }
                }
                // difficult mode. efficient computer strategy
            } else if (compMode == 1) {
                var reroll = 2
                when (reroll){
                    1,2 -> {
                        if (meTotalScore > compTotalScore) {
                            if (score(compDiceValues) < 20) {
                                for (i in compDiceValues) {
                                        //reroll
                                        val randomValue = IntArray(5)
                                        for (i in randomValue.indices) {
                                            randomValue[i] = random.nextInt(6) + 1
                                        }
                                        for (i in randomValue.indices) {
                                            compDiceBtn[i].setImageResource(diceImages[randomValue[i] - 1])
                                            rotateAnimation(compDiceBtn[i])
                                        }

                                        reroll--
                                }
                            }
                        }
                    }
                }

            }

            //tally up the scores
            meTotalScore += score(meDiceValues)
            compTotalScore += score(compDiceValues)

            //user sees updated score and round
            scoreTextView.text = "Score\nH:${meTotalScore} /C:${compTotalScore} "
            rounds++
            roundsTextView.text = "Round ${rounds}"

            //restart roll count
            rollsLeft = 3
            throwBtn.text = "Throw"

            //https://stackoverflow.com/questions/45213706/kotlin-wait-function
            //take off highlight from button
            Handler(Looper.getMainLooper()).postDelayed(
                {
                    //removes dices from the selected dices list
                    for (element in meDiceBtn){
                        element.setBackgroundResource(R.drawable.deselect_dice)
                        selectedDices.remove(element)
                    }
                    for (element in compDiceBtn){
                        element.setBackgroundResource(R.drawable.deselect_dice)
                        selectedCompDices.remove(element)
                    }
                },
                1500 // value in milliseconds
            )


            //determine who wins

            //tie
            if (winningScore == meTotalScore && winningScore == compTotalScore) {
                Toast.makeText(this, "Its a tie! Keep throwing", Toast.LENGTH_SHORT).show()
                //allow for one more throw, no rerolls in this round
                throwBtn.setOnClickListener {
                    if (tieRound == 1) {
                            // Roll 5 dices for player 1
                            throwDice(meDiceValues, meDiceBtn)
                            // Roll 5 dices for player 2
                            throwDice(compDiceValues, compDiceBtn)
                    }
                    tieRound--
                    throwBtn.isEnabled = false
                }
                //if human wins, display you win pop up
            } else if (meTotalScore >= winningScore && meTotalScore > compTotalScore) {
                meWins++
                val dialogBinder = layoutInflater.inflate(R.layout.win_popup, null)

                val myDialog = Dialog(this)
                myDialog.setContentView(dialogBinder)
                myDialog.setCancelable(false)
                myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                myDialog.show()

                val backBtn = dialogBinder.findViewById<Button>(R.id.backBtn)
                backBtn.setOnClickListener {
                    var intent = Intent(this, MainActivity::class.java)
                    //send information to mainActivity. send the number of wins
                    intent.putExtra("humanwin", meWins.toString())
                    intent.putExtra("compwin", compWins.toString())
                    startActivity(intent)
                    finish()
                }

                //if computer wins, display you lose pop up
            } else if(compTotalScore >= winningScore) {
                compWins++
                val dialogBinder = layoutInflater.inflate(R.layout.lose_popup, null)

                val myDialog = android.app.Dialog(this)
                myDialog.setContentView(dialogBinder)
                myDialog.setCancelable(false)
                myDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                myDialog.show()

                val backBtn = dialogBinder.findViewById<Button>(R.id.backBtn)
                backBtn.setOnClickListener {
                    var intent = Intent(this, MainActivity::class.java)
                    //send information to mainActivity. send the number of wins
                    intent.putExtra("humanwin", meWins.toString())
                    intent.putExtra("compwin", compWins.toString())
                    startActivity(intent)
                    finish()
                }
            }

            tieRound = 1
            throwBtn.isEnabled = true
            //update wins
            winsTextView.text = "Wins\nH:${meWins} /C:${compWins}"
        }


        //orientation change
        //restore data to the previous state using the data stored in the bundle
        if(savedInstanceState != null) {
            meTotalScore = savedInstanceState.getInt("metotal")
            compTotalScore = savedInstanceState.getInt("comptotal")
            rounds = savedInstanceState.getInt("rounds")
            meWins = savedInstanceState.getInt("pwon")
            compWins = savedInstanceState.getInt("cwon")

            scoreTextView.text = "Score\nH:${meTotalScore} /C:${compTotalScore} "
            roundsTextView.text = "Round ${rounds}"
            winsTextView.text = "Wins\nH:${meWins} /C:${compWins}"

        }
    }


    //function to throw dice. rolls 5 dice of the player and generates random dice output
    private fun throwDice(diceValues: IntArray, diceBtn: Array<ImageButton>) {
        for (i in diceValues.indices) {
            diceValues[i] = random.nextInt(6) + 1
        }
        for (i in diceValues.indices) {
            diceBtn[i].setImageResource(diceImages[diceValues[i] - 1])
            rotateAnimation(diceBtn[i])
        }
    }

    //function to add up the 5 dice for a round's score
    private fun score(arr: IntArray): Int {
        var sum = 0

        for (i in 0 until 5) {
            sum += arr[i]
        }
        return sum
    }

    //highlighting and unhighlighting dices. adding and removing them to/from a mutable list
    fun selectDice(diceBtn: Array<ImageButton>){
        for (i in diceBtn){
            i.setOnClickListener{
                if (i.background.constantState == ContextCompat.getDrawable(this, R.drawable.select_dice)?.constantState) {
                    // If the button is already highlighted, set the original drawable
                    i.background = ContextCompat.getDrawable(this, R.drawable.deselect_dice)
                    selectedDices.remove(i)
                } else {
                    // If the button is not highlighted, set the highlight drawable
                    i.background = ContextCompat.getDrawable(this, R.drawable.select_dice)
                    selectedDices.add(i)
                }
            }
        }
    }

    //function for computer following the random strategy.
    private fun computerRandomStrategy(compDiceBtn: Array<ImageButton>, diceVal: IntArray){
        val selectdice = random.nextInt(5)+1
        for (i in 0 until selectdice) compDiceBtn[i].setBackgroundResource(R.drawable.select_dice)

        for (i in compDiceBtn){
            //if dice had been selected, add it to the selected computer dice list
            if (i.background.constantState == ContextCompat.getDrawable(this, R.drawable.select_dice)?.constantState) {
                selectedCompDices.add(i)
            }
        }

        val compSelectedDices = compDiceBtn.filter { ibtn-> selectedCompDices.none{yitem->yitem.id == ibtn.id}}
        for (i in compSelectedDices.indices) {
            diceVal[i] = random.nextInt(6) + 1
        }
        for (i in compSelectedDices.indices) {
            compSelectedDices[i].setImageResource(diceImages[diceVal[i] - 1])
            rotateAnimation(compSelectedDices[i])
        }
    }

    //animating the dice rotating when rolling
    private fun rotateAnimation(dice: ImageButton){
        val rotateDice = AnimationUtils.loadAnimation(this,R.anim.rotate)
        dice.startAnimation(rotateDice)
    }

    //store data in the bundle so it can be passed into the onCreate method of every gameActivity
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putInt("metotal", meTotalScore)
        outState.putInt("comptotal", compTotalScore)
        outState.putInt("rounds", rounds)
        outState.putInt("pwon", meWins)
        outState.putInt("cwon", compWins)
    }

}

//strategy for computer optimum
//it only checks when the human player is leading. When human player is leading, the computer checks the sum of the dice values
//currently thrown. if its less than 20, it will reroll the dice
