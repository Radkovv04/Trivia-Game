package com.example.bulgariatriviaconquest

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class QuestionActivity : AppCompatActivity() {

    private var selectedAnswer: String? = null
    private var selectedButton: Button? = null
    private var isRedTurn = true
    private var redCorrect = false
    private var blueCorrect = false
    private var correctAnswerText: String = ""
    var redTargetRegion: String? = null
    var blueTargetRegion: String? = null
    var redLastResult: Boolean = false
    var blueLastResult: Boolean = false

    // Player name variables
    private lateinit var redName: String
    private lateinit var blueName: String

    private var countDownTimer: CountDownTimer? = null
    private var isAnswered = false // Prevents clicking after time/submit


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question)

        val currentQuestion = QuestionBank.getNextQuestion()

        val questionTextView = findViewById<TextView>(R.id.questionTextView)
        val timerTextView = findViewById<TextView>(R.id.timerTextView) // ADD THIS TO YOUR XML!
        val buttons = listOf(
            findViewById<Button>(R.id.btnOpt1),
            findViewById<Button>(R.id.btnOpt2),
            findViewById<Button>(R.id.btnOpt3),
            findViewById<Button>(R.id.btnOpt4)
        )
        // Find your top banner text view (make sure the ID matches your XML)
        val turnIndicatorTextView = findViewById<TextView>(R.id.turnIndicatorTextView)
        val isRedTurn = intent.getBooleanExtra("IS_RED_TURN", true)

// Update the text and color based on whose turn it is
        if (isRedTurn) {
            turnIndicatorTextView.text = "Ред на Червените"
            turnIndicatorTextView.setTextColor(Color.RED)
        } else {
            turnIndicatorTextView.text = "Ред на Сините"
            turnIndicatorTextView.setTextColor(Color.parseColor("#4169E1")) // Royal Blue
        }

        // Set texts
        questionTextView.text = currentQuestion.questionText
        correctAnswerText = currentQuestion.options[currentQuestion.correctAnswerIndex]

        buttons.forEachIndexed { index, button ->
            button.text = currentQuestion.options[index]

            // --- SELECTION LOGIC ---
            button.setOnClickListener {
                if (isAnswered) return@setOnClickListener

                // Clear previous selection visuals
                buttons.forEach { it.setBackgroundResource(R.drawable.button_normal) }

                selectedButton = button

                // If your button text is "A: София", this gets just "София"
                selectedAnswer = button.text.toString().substringAfter(": ")

                // Apply your glow/highlight
                button.setBackgroundColor(Color.CYAN)
            }
        }

        // --- TIMER LOGIC (20 Seconds) ---
        countDownTimer = object : CountDownTimer(20000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                timerTextView.text = "$secondsRemaining"

                // Turn red if less than 5 seconds remain
                if (secondsRemaining <= 5) {
                    timerTextView.setTextColor(Color.RED)
                    // Optional: Make it slightly larger to add tension
                    timerTextView.textSize = 28f
                } else {
                    // Keep it dark brown/black otherwise
                    timerTextView.setTextColor(Color.parseColor("#FFFFFF"))
                    timerTextView.textSize = 24f
                }
            }

            override fun onFinish() {
                if (!isAnswered) {
                    revealAndFinish(false)
                }
            }
        }.start()

        val confirmButton = findViewById<Button>(R.id.submitButton)
        confirmButton.setOnClickListener {
            if (selectedAnswer == null) {
                Toast.makeText(this, "Моля, изберете отговор!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val isCorrect = (selectedAnswer == correctAnswerText)
            revealAndFinish(isCorrect)
        }
        countDownTimer = object : CountDownTimer(20000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                val timerView = findViewById<TextView>(R.id.timerTextView)
                timerView?.text = seconds.toString()
                if (seconds <= 5) timerView?.setTextColor(Color.RED)
            }
            override fun onFinish() {
                revealAndFinish(false)
            }
        }.start()
    }

    // --- REVEAL LOGIC ---
    private fun revealAndFinish(isCorrect: Boolean) {
        isAnswered = true
        countDownTimer?.cancel()

        // Find all buttons again to color them
        val buttons = listOf<Button>(
            findViewById(R.id.btnOpt1), findViewById(R.id.btnOpt2),
            findViewById(R.id.btnOpt3), findViewById(R.id.btnOpt4)
        )

        buttons.forEach { button ->
            if (button.text == correctAnswerText) {
                button.setBackgroundColor(Color.GREEN) // Always show correct answer in green
            } else if (button == selectedButton && !isCorrect) {
                button.setBackgroundColor(Color.RED) // Show user's wrong answer in red
            }
        }

        // Wait 2 seconds so the user can see the answer, then go back
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            val resultIntent = Intent()
            // We just send "WAS_CORRECT". MainActivity knows if it came from Red or Blue.
            resultIntent.putExtra("WAS_CORRECT", isCorrect)

            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }, 2000)
    }

    private fun updateTurnDisplay(turnText: TextView) {
        if (isRedTurn) {
            turnText.text = "Ред на $redName"
            turnText.setTextColor(Color.RED)
        } else {
            turnText.text = "Ред на $blueName"
            turnText.setTextColor(Color.parseColor("#4169E1")) // Blue
        }
    }

    private fun applyCustomGlow(button: Button, colorCode: String) {
        val shape = GradientDrawable()
        shape.cornerRadius = 100f
        shape.setStroke(8, Color.parseColor(colorCode))
        shape.setColor(Color.parseColor("#26FFFFFF")) // Subtle highlight
        button.background = shape
    }
}