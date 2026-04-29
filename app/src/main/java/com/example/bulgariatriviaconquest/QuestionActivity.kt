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

        // Define the specific glowing colors for A(Red), B(Green), C(Blue), D(Yellow)
        // We use #88 (about 50% opacity) so your background image still shows through!
        val selectionColors = listOf(
            Color.parseColor("#88FF0000"), // Slot 1: Glowing Red
            Color.parseColor("#8800FF00"), // Slot 2: Glowing Green
            Color.parseColor("#880000FF"), // Slot 3: Glowing Blue
            Color.parseColor("#88FFD700")  // Slot 4: Glowing Gold/Yellow
        )

        // Map the XML shapes to the buttons (A=Red, B=Green, C=Blue, D=Yellow)
        // 0 = A (Red), 1 = B (Green), 2 = C (Blue), 3 = D (Yellow)
        val highlightDrawables = listOf(
            R.drawable.glow_red,
            R.drawable.glow_green,
            R.drawable.glow_blue,
            R.drawable.glow_yellow
        )

        buttons.forEachIndexed { index, button ->
            button.text = "        ${currentQuestion.options[index]}"

            button.setOnClickListener {
                if (isAnswered) return@setOnClickListener

                // Reset all buttons to transparent
                buttons.forEach { it.setBackgroundResource(android.R.color.transparent) }

                selectedButton = button
                selectedAnswer = button.text.toString()

                // Apply the specific color based on its index (0, 1, 2, or 3)
                button.setBackgroundResource(highlightDrawables[index])
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

            // THIS IS THE FIX. It removes the spaces before checking if they won!
            val isCorrect = (selectedButton?.text.toString().trim() == correctAnswerText)
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

        val buttons = listOf<Button>(
            findViewById(R.id.btnOpt1), findViewById(R.id.btnOpt2),
            findViewById(R.id.btnOpt3), findViewById(R.id.btnOpt4)
        )

        // THE FIX: Add .trim() here so it ignores the spaces we added!
        val correctBtn = buttons.find { it.text.toString().trim() == correctAnswerText }

        // UPGRADE: If they got it wrong, instantly turn their bad choice Red!
        if (!isCorrect && selectedButton != correctBtn) {
            selectedButton?.setBackgroundResource(R.drawable.glow_red)
        }

        // --- THE PRO MILLIONAIRE ANIMATION ---
        var blinkCount = 0
        val blinkHandler = android.os.Handler(android.os.Looper.getMainLooper())

        val blinkRunnable = object : Runnable {
            override fun run() {
                if (blinkCount < 6) {
                    // Blink the CORRECT answer Green to reveal it
                    if (blinkCount % 2 == 0) {
                        correctBtn?.setBackgroundResource(R.drawable.glow_green)
                    } else {
                        correctBtn?.setBackgroundResource(android.R.color.transparent)
                    }

                    blinkCount++
                    blinkHandler.postDelayed(this, 250)
                } else {
                    // Lock the correct answer in Green
                    correctBtn?.setBackgroundResource(R.drawable.glow_green)

                    // Wait 2.5 seconds to feel the pain/joy, then return to map
                    blinkHandler.postDelayed({
                        val resultIntent = Intent()

                        val isRedTurn = intent.getBooleanExtra("IS_RED_TURN", true)
                        if (isRedTurn) {
                            resultIntent.putExtra("RED_CORRECT", isCorrect)
                        } else {
                            resultIntent.putExtra("BLUE_CORRECT", isCorrect)
                        }
                        resultIntent.putExtra("WAS_CORRECT", isCorrect)

                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    }, 2500)
                }
            }
        }

        // Start the blinking!
        blinkRunnable.run()
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