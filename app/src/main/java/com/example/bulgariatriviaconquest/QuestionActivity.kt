package com.example.bulgariatriviaconquest

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class QuestionActivity : AppCompatActivity() {

    private var selectedAnswer: String? = null
    private var selectedButton: Button? = null
    private var correctAnswerText: String = ""
    private var countDownTimer: CountDownTimer? = null
    private var isAnswered = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question)

        //UI buttons
        val btnHelp = findViewById<ImageView>(R.id.btnHelp)
        val helpOverlay = findViewById<View>(R.id.helpOverlay)
        val btnCloseHelp = findViewById<View>(R.id.btnCloseHelp)
        btnHelp.setOnClickListener {
            helpOverlay.visibility = View.VISIBLE
        }
        btnCloseHelp.setOnClickListener {
            helpOverlay.visibility = View.GONE
        }
        helpOverlay.setOnClickListener {
            helpOverlay.visibility = View.GONE
        }

        val btnSettings = findViewById<ImageView>(R.id.btnSettings)
        val settingsOverlay = findViewById<View>(R.id.settingsOverlay)
        val btnCloseSettings = findViewById<View>(R.id.btnCloseSettings)

        val switchAudio = findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchAudio)
        val switchGraphics = findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchGraphics)

        btnSettings.setOnClickListener { settingsOverlay.visibility = View.VISIBLE }
        btnCloseSettings.setOnClickListener { settingsOverlay.visibility = View.GONE }

        settingsOverlay.setOnClickListener { settingsOverlay.visibility = View.GONE }

        switchAudio.isChecked = GameManager.isAudioEnabled
        switchAudio.text = if (GameManager.isAudioEnabled) "ЗВУК: ВКЛЮЧЕН" else "ЗВУК: ИЗКЛЮЧЕН"
        switchAudio.setOnCheckedChangeListener { _, isChecked ->
            GameManager.isAudioEnabled = isChecked
            switchAudio.text = if (isChecked) "ЗВУК: ВКЛЮЧЕН" else "ЗВУК: ИЗКЛЮЧЕН"
        }
        switchGraphics.setOnCheckedChangeListener { _, isChecked ->
            switchGraphics.text = if (isChecked) "ГРАФИКА: ВИСОКА" else "ГРАФИКА: НИСКА"
        }
        // End of UI buttons
        val tvRedTeamName = findViewById<TextView>(R.id.redTeamName)
        val tvBlueTeamName = findViewById<TextView>(R.id.blueTeamName)
        tvRedTeamName.text = GameManager.playerUsername
        tvBlueTeamName.text = GameManager.playerUsername
        val currentQuestion = QuestionBank.getNextQuestion(GameManager.isHardMode)

        val questionTextView = findViewById<TextView>(R.id.questionTextView)
        val timerTextView = findViewById<TextView>(R.id.timerTextView)
        val buttons = listOf(
            findViewById<Button>(R.id.btnOpt1),
            findViewById<Button>(R.id.btnOpt2),
            findViewById<Button>(R.id.btnOpt3),
            findViewById<Button>(R.id.btnOpt4)
        )
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Toast.makeText(this@QuestionActivity, "Не можеш да избягаш от битката!", Toast.LENGTH_SHORT).show()
            }
        })
        val turnIndicatorTextView = findViewById<TextView>(R.id.turnIndicatorTextView)
        val isRedTurn = intent.getBooleanExtra("IS_RED_TURN", true)
        if (isRedTurn) {
            turnIndicatorTextView.text = "Ред на Червените"
            turnIndicatorTextView.setTextColor(Color.RED)
        } else {
            turnIndicatorTextView.text = "Ред на Сините"
            turnIndicatorTextView.setTextColor(Color.parseColor("#4169E1")) // Royal Blue
        }
        questionTextView.text = currentQuestion.questionText
        correctAnswerText = currentQuestion.options[currentQuestion.correctAnswerIndex]

<<<<<<< Updated upstream
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
=======
>>>>>>> Stashed changes
        val highlightDrawables = listOf(
            R.drawable.glow_red,
            R.drawable.glow_green,
            R.drawable.glow_blue,
            R.drawable.glow_yellow
        )
<<<<<<< Updated upstream

        buttons.forEachIndexed { index, button ->
            button.text = "        ${currentQuestion.options[index]}"

            button.setOnClickListener {
                if (isAnswered) return@setOnClickListener

                // Reset all buttons to transparent
                buttons.forEach { it.setBackgroundResource(android.R.color.transparent) }

                selectedButton = button
                selectedAnswer = button.text.toString()

                // Apply the specific color based on its index (0, 1, 2, or 3)
=======

        buttons.forEachIndexed { index, button ->
            button.text = "        ${currentQuestion.options[index]}"
            button.setOnClickListener {
                if (isAnswered) return@setOnClickListener
                buttons.forEach { it.setBackgroundResource(android.R.color.transparent) }
                selectedButton = button
                selectedAnswer = button.text.toString()
>>>>>>> Stashed changes
                button.setBackgroundResource(highlightDrawables[index])
            }
        }
        // TIMER LOGIC (20 Seconds)
        countDownTimer = object : CountDownTimer(20000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                timerTextView.text = "$secondsRemaining"

                if (secondsRemaining <= 5) {
                    timerTextView.setTextColor(Color.RED)
                    timerTextView.textSize = 28f
                } else {
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
<<<<<<< Updated upstream

            // THIS IS THE FIX. It removes the spaces before checking if they won!
=======
>>>>>>> Stashed changes
            val isCorrect = (selectedButton?.text.toString().trim() == correctAnswerText)
            revealAndFinish(isCorrect)
        }
        fun updatePointsDisplay() {
            val tvRedPoints = findViewById<TextView>(R.id.redScoreText)
            val tvBluePoints = findViewById<TextView>(R.id.blueScoreText)
            tvRedPoints.text = GameManager.redPoints.toString()
            tvBluePoints.text = GameManager.bluePoints.toString()
        }
        updatePointsDisplay()
    }private fun revealAndFinish(isCorrect: Boolean) {
        isAnswered = true
        countDownTimer?.cancel()
<<<<<<< Updated upstream

=======
>>>>>>> Stashed changes
        val buttons = listOf<Button>(
            findViewById(R.id.btnOpt1), findViewById(R.id.btnOpt2),
            findViewById(R.id.btnOpt3), findViewById(R.id.btnOpt4)
        )
        val correctBtn = buttons.find { it.text.toString().trim() == correctAnswerText }
        if (!isCorrect && selectedButton != correctBtn) {
            selectedButton?.setBackgroundResource(R.drawable.glow_red)
        }
        var blinkCount = 0
        val blinkHandler = android.os.Handler(android.os.Looper.getMainLooper())
        val blinkRunnable = object : Runnable {
            override fun run() {
                if (blinkCount < 6) {
                    if (blinkCount % 2 == 0) {
                        correctBtn?.setBackgroundResource(R.drawable.glow_green)
                    } else {
                        correctBtn?.setBackgroundResource(android.R.color.transparent)
                    }
                    blinkCount++
                    blinkHandler.postDelayed(this, 150)
                } else {
                    correctBtn?.setBackgroundResource(R.drawable.glow_green)

<<<<<<< Updated upstream
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
                    blinkHandler.postDelayed(this, 150)
                } else {
                    // Lock the correct answer in Green
                    correctBtn?.setBackgroundResource(R.drawable.glow_green)

                    // Wait 2.5 seconds to feel the pain/joy, then return to map
                    blinkHandler.postDelayed({
                        val resultIntent = Intent()

                        val isRedTurn = intent.getBooleanExtra("IS_RED_TURN", true)
=======
                    blinkHandler.postDelayed({
                        val resultIntent = Intent()
                        val isRedTurn = intent.getBooleanExtra("IS_RED_TURN", true)

>>>>>>> Stashed changes
                        if (isRedTurn) {
                            resultIntent.putExtra("RED_CORRECT", isCorrect)
                        } else {
                            resultIntent.putExtra("BLUE_CORRECT", isCorrect)
                        }
                        resultIntent.putExtra("WAS_CORRECT", isCorrect)

                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    }, 1500)
                }
            }
        }
<<<<<<< Updated upstream

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
=======
        blinkRunnable.run()
>>>>>>> Stashed changes
    }
}