package com.example.bulgariatriviaconquest

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    // We only need ONE variable to track who is currently answering the question
    private var isRedTurnToAnswer = true

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        GameManager.startNewGame()
        updateMapVisuals()
        showPhaseBanner(GamePhase.CAPTURE)

        val mapImageView = findViewById<ImageView>(R.id.bulgariaMap)
        mapImageView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                v.performClick()
                val bitmap = (mapImageView.drawable as BitmapDrawable).bitmap
                val x = (event.x * bitmap.width / v.width).toInt()
                val y = (event.y * bitmap.height / v.height).toInt()

                if (x in 0 until bitmap.width && y in 0 until bitmap.height) {
                    val pixel = bitmap.getPixel(x, y)
                    val hexColor = String.format("#%06X", (0xFFFFFF and pixel))

                    val regionName = when (hexColor.uppercase()) {
                        "#FAF7E6" -> "Видин"
                        "#F9F6E3" -> "Плевен"
                        "#FAF7E8" -> "Русе"
                        "#FAF6EB" -> "Варна"
                        "#FBF6E2" -> "София"
                        "#FCF6E0" -> "Ловеч"
                        "#FCF7E4" -> "Стара Загора"
                        "#FAF6ED" -> "Бургас"
                        "#FBF6E3" -> "Благоевград"
                        "#F9F6E5" -> "Пловдив"
                        "#FAF6EA" -> "Хасково"
                        "#F9F6E7" -> "Смолян"
                        else -> null
                    }
                    if (regionName != null) handleTerritoryClick(regionName)
                }
            }
            true
        }
    }
    private fun getFlagId(regionName: String): Int? {
        return when(regionName) {
            "Видин" -> R.id.flagVidin
            "Плевен" -> R.id.flagPleven
            "Русе" -> R.id.flagRuse
            "Варна" -> R.id.flagVarna
            "София" -> R.id.flagSofia
            "Ловеч" -> R.id.flagLovech
            "Стара Загора" -> R.id.flagStaraZagora
            "Бургас" -> R.id.flagBurgas
            "Благоевград" -> R.id.flagBlagoevgrad
            "Пловдив" -> R.id.flagPlovdiv
            "Хасково" -> R.id.flagHaskovo
            "Смолян" -> R.id.flagSmolyan
            else -> null
        }
    }
    private fun hideAllFlags() {
        val allFlagIds = listOf(
            R.id.flagVidin, R.id.flagPleven, R.id.flagRuse, R.id.flagVarna,
            R.id.flagSofia, R.id.flagLovech, R.id.flagStaraZagora, R.id.flagBurgas,
            R.id.flagBlagoevgrad, R.id.flagPlovdiv, R.id.flagHaskovo, R.id.flagSmolyan
        )

        for (id in allFlagIds) {
            findViewById<ImageView>(id)?.visibility = View.GONE
        }
    }

    private fun updateMapVisuals() {
        // 1. Color the territory overlays based on owner
        for ((name, territory) in GameManager.territories) {
            val overlayId = when(name) {
                "Видин" -> R.id.overlayVidin
                "Плевен" -> R.id.overlayPleven
                "Русе" -> R.id.overlayRuse
                "Варна" -> R.id.overlayVarna
                "София" -> R.id.overlaySofia
                "Ловеч" -> R.id.overlayLovech
                "Стара Загора" -> R.id.overlayStaraZagora
                "Бургас" -> R.id.overlayBurgas
                "Благоевград" -> R.id.overlayBlagoevgrad
                "Пловдив" -> R.id.overlayPlovdiv
                "Хасково" -> R.id.overlayHaskovo
                "Смолян" -> R.id.overlaySmolyan
                else -> null
            }

            overlayId?.let { id ->
                val overlayView = findViewById<ImageView>(id)
                if (territory.owner != Team.NEUTRAL) {
                    overlayView.visibility = View.VISIBLE
                    val color = if (territory.owner == Team.RED) Color.RED else Color.parseColor("#4169E1")
                    overlayView.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
                    overlayView.alpha = 0.5f
                } else {
                    overlayView.visibility = View.INVISIBLE
                }
            }
        }
        hideAllFlags()

        // 3. Clear targets so the game knows we are ready for a new round
        GameManager.redTargetRegion = null
        GameManager.blueTargetRegion = null
        findViewById<TextView>(R.id.redScoreText)?.text = GameManager.redPoints.toString()
        findViewById<TextView>(R.id.blueScoreText)?.text = GameManager.bluePoints.toString()


    }

    private fun handleTerritoryClick(regionName: String) {
        val currentPlayer = GameManager.currentTurn
        val result = GameManager.checkMove(regionName, currentPlayer)

        when (result) {
            GameManager.MoveResult.VALID -> {
                if (currentPlayer == Team.RED) {
                    GameManager.redTargetRegion = regionName
                    showFlag(regionName, Team.RED)
                    GameManager.currentTurn = Team.BLUE
                    Toast.makeText(this, "Ред на Сините!", Toast.LENGTH_SHORT).show()
                } else {
                    GameManager.blueTargetRegion = regionName
                    showFlag(regionName, Team.BLUE)
                    launchQuestionPhase()
                }
            }
            else -> Toast.makeText(this, "Невалиден ход!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showFlag(regionName: String, team: Team) {
        val flagId = getFlagId(regionName)
        flagId?.let {
            val flagView = findViewById<ImageView>(it)
            flagView?.visibility = View.VISIBLE
            val color = if (team == Team.RED) Color.RED else Color.parseColor("#4169E1")
            flagView?.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)
        }
    }

    private fun launchQuestionPhase() {
        isRedTurnToAnswer = true // Start the phase with Red answering
        startQuestionActivity()
    }

    private fun startQuestionActivity() {
        val intent = Intent(this, QuestionActivity::class.java)
        // Pass the turn info to QuestionActivity so it can change its title/UI
        intent.putExtra("IS_RED_TURN", isRedTurnToAnswer)
        startActivityForResult(intent, 1001)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            val wasCorrect = data?.getBooleanExtra("WAS_CORRECT", false) ?: false

            if (isRedTurnToAnswer) {
                // Red finished their question
                GameManager.redLastResult = wasCorrect
                isRedTurnToAnswer = false

                // Immediately launch the screen again for Blue
                startQuestionActivity()
            } else {
                // Blue finished their question
                GameManager.blueLastResult = wasCorrect

                // 1. Remember the phase BEFORE we resolve the round
                val phaseBeforeRound = GameManager.currentPhase

                // 2. Resolve the points and territory ownership
                GameManager.resolveRound(GameManager.redLastResult, GameManager.blueLastResult)

                // 3. Clean up the map, hide flags, update score texts
                updateMapVisuals()

                // 4. Did the phase just change to ATTACK? Show the popup!
                if (phaseBeforeRound == GamePhase.CAPTURE && GameManager.currentPhase == GamePhase.ATTACK) {
                    showPhaseBanner(GamePhase.ATTACK)
                }

                // 5. Check for Game Over!
                // The game ends IF an instant winner is declared (Kingdom captured)
                // OR if one team successfully conquers 100% of the map
                val redOwnsAll = GameManager.territories.values.all { it.owner == Team.RED }
                val blueOwnsAll = GameManager.territories.values.all { it.owner == Team.BLUE }

                if (GameManager.instantWinner != null || redOwnsAll || blueOwnsAll) {
                    showWinnerScreen()
                } else {
                    // Game continues
                    GameManager.currentTurn = Team.RED
                }
            }
        }
    }
    private fun showPhaseBanner(phase: GamePhase) {
        val phaseOverlay = findViewById<View>(R.id.phaseOverlayLayout)
        val phaseImage = findViewById<ImageView>(R.id.phaseImage)

        // Set the correct image
        if (phase == GamePhase.CAPTURE) {
            phaseImage.setImageResource(R.drawable.attack_phase)
        } else {
            phaseImage.setImageResource(R.drawable.capture_phase)
        }

        // Show it
        phaseOverlay.visibility = View.VISIBLE

        // Hide it automatically after 3 seconds
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            phaseOverlay.visibility = View.GONE
        }, 3000)
    }
    private fun showWinnerScreen() {
        val winnerOverlay = findViewById<View>(R.id.winnerOverlayLayout)
        val winnerText = findViewById<TextView>(R.id.winnerText)

        // Show the overlay
        winnerOverlay.visibility = View.VISIBLE

        // Determine who won based on points
        if (GameManager.redPoints > GameManager.bluePoints) {
            winnerText.text = "Победител:\nЧЕРВЕНИТЕ"
            winnerText.setTextColor(Color.RED)
        } else if (GameManager.bluePoints > GameManager.redPoints) {
            winnerText.text = "Победител:\nСИНИТЕ"
            winnerText.setTextColor(Color.parseColor("#4169E1"))
        } else {
            winnerText.text = "РАВЕНСТВО!"
            winnerText.setTextColor(Color.WHITE)
        }

        // Set up the 3 buttons
        findViewById<Button>(R.id.btnPlayAgain).setOnClickListener {
            // This explicitly tells Android to open HomeActivity and clear the old game out of memory
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish() // Closes the current Map Activity completely
        }

        findViewById<Button>(R.id.btnRematch).setOnClickListener {
            winnerOverlay.visibility = View.GONE
            GameManager.startNewGame()
            updateMapVisuals()
            Toast.makeText(this, "Нова игра започва!", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnQuit).setOnClickListener {
            finishAffinity() // Closes the entire app immediately
        }
    }
}