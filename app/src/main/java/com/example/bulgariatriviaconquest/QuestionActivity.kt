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
                buttons.forEach { it.setBackgroundResource(android.R.color.transparent) }
                selectedButton = button
                selectedAnswer = button.text.toString()
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
                    }, 1500)
                }
            }
        }
        blinkRunnable.run()
    }
}