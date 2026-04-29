package com.example.bulgariatriviaconquest

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs

class TieBreakerActivity : AppCompatActivity() {

    private var correctAnswer = 0
    private var isRedTurn = true

    private var redFinalAnswer = 0
    private var blueFinalAnswer = 0
    private var redTimeTaken = 0L
    private var blueTimeTaken = 0L
    private var turnStartTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tiebreaker)

        val turnIndicator = findViewById<TextView>(R.id.duelTurnIndicator)
        val questionText = findViewById<TextView>(R.id.duelQuestionText)
        val redInput = findViewById<EditText>(R.id.redInput)
        val blueInput = findViewById<EditText>(R.id.blueInput)
        val submitBtn = findViewById<Button>(R.id.btnSubmitDuel)

        // Load Question
        val currentQuestion = QuestionBank.getNextNumericQuestion()
        questionText.text = currentQuestion.questionText
        correctAnswer = currentQuestion.correctAnswer

        // Start Red's Turn
        blueInput.isEnabled = false
        startTurn(redInput)

        submitBtn.setOnClickListener {
            if (isRedTurn) {
                val inputStr = redInput.text.toString()
                if (inputStr.isEmpty()) {
                    Toast.makeText(this, "Въведете число!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                redFinalAnswer = inputStr.toInt()
                redTimeTaken = System.currentTimeMillis() - turnStartTime

                // Hide Red's answer with asterisks
                redInput.setText("***")
                redInput.isEnabled = false

                // Start Blue's Turn
                isRedTurn = false
                turnIndicator.text = "РЕД НА СИНИТЕ!"
                turnIndicator.setTextColor(Color.parseColor("#4169E1"))

                blueInput.isEnabled = true
                startTurn(blueInput)

            } else {
                val inputStr = blueInput.text.toString()
                if (inputStr.isEmpty()) {
                    Toast.makeText(this, "Въведете число!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                blueFinalAnswer = inputStr.toInt()
                blueTimeTaken = System.currentTimeMillis() - turnStartTime
                blueInput.isEnabled = false
                submitBtn.isEnabled = false

                hideKeyboard()

                // THE DRAMATIC REVEAL PHASE
                turnIndicator.text = "СРАВНЯВАНЕ..."
                turnIndicator.setTextColor(Color.BLACK)

                // Show both answers immediately
                redInput.setText(redFinalAnswer.toString())

                // Wait 2.5 seconds to build tension, then show the winner!
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    resolveDuel(questionText, redInput, blueInput)
                }, 2500)
            }
        }
    }

    private fun startTurn(inputBox: EditText) {
        inputBox.requestFocus()
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(inputBox, InputMethodManager.SHOW_IMPLICIT)
        turnStartTime = System.currentTimeMillis()
    }

    private fun hideKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun resolveDuel(questionText: TextView, redInput: EditText, blueInput: EditText) {
        questionText.text = "Верният отговор е: $correctAnswer"

        val redDiff = abs(correctAnswer - redFinalAnswer)
        val blueDiff = abs(correctAnswer - blueFinalAnswer)
        val winner: Team

        if (redDiff < blueDiff) {
            winner = Team.RED
        } else if (blueDiff < redDiff) {
            winner = Team.BLUE
        } else {
            // TIE BREAKER BY SPEED
            winner = if (redTimeTaken <= blueTimeTaken) Team.RED else Team.BLUE
            Toast.makeText(this, "РАВЕНСТВО! Бързината решава!", Toast.LENGTH_LONG).show()
        }

        if (winner == Team.RED) {
            redInput.setBackgroundColor(Color.GREEN)
            blueInput.setBackgroundColor(Color.RED)
        } else {
            blueInput.setBackgroundColor(Color.GREEN)
            redInput.setBackgroundColor(Color.RED)
        }

        // Wait 3 seconds, then return to map
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            val resultIntent = Intent()
            resultIntent.putExtra("WINNING_TEAM", winner.name)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }, 3000)
    }
}