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
    private var isFirstAnswerer = true

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
    private fun handleEndOfAttackTurn() {
        GameManager.attackTurnsCompleted++ // A team just finished their attack

        // If it divides evenly by 2, it means BOTH teams have played!
        if (GameManager.attackTurnsCompleted % 2 == 0) {
            GameManager.currentRound++
            showRoundBanner()
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
        val sofia = GameManager.territories["София"]
        val burgas = GameManager.territories["Бургас"]
        val castleSofia = findViewById<ImageView>(R.id.castleSofia)
        val castleBurgas = findViewById<ImageView>(R.id.castleBurgas)

        // Red Kingdom (Sofia)
        sofia?.let {
            if (it.isKingdom) {
                when (it.lives) {
                    2 -> castleSofia.setImageResource(R.drawable.castle_red)
                    1 -> castleSofia.setImageResource(R.drawable.castle_red_destroyed)
                    0 -> castleSofia.visibility = View.GONE
                }
            }
        }

        // Blue Kingdom (Burgas)
        burgas?.let {
            if (it.isKingdom) {
                when (it.lives) {
                    2 -> castleBurgas.setImageResource(R.drawable.castle_blue)
                    1 -> castleBurgas.setImageResource(R.drawable.castle_blue_destroyed)
                    0 -> castleBurgas.visibility = View.GONE
                }
            }
        }

        // Reset targets and update scores
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
                if (GameManager.currentPhase == GamePhase.CAPTURE) {
                    val neutralCount = GameManager.territories.values.count { it.owner == Team.NEUTRAL }

                    // --- THE DEADLOCK FIX (Instantly force both to answer) ---
                    if (neutralCount == 1) {
                        GameManager.redTargetRegion = regionName
                        GameManager.blueTargetRegion = regionName
                        showFlag(regionName, Team.RED) // Show Red flag just for visual feedback

                        isRedTurnToAnswer = true // Red answers first
                        startQuestionActivity() // LAUNCH IMMEDIATELY. Do not wait for Blue to click!
                        return
                    }

                    // --- NORMAL CAPTURE PHASE ---
                    if (currentPlayer == Team.RED) {
                        GameManager.redTargetRegion = regionName
                        showFlag(regionName, Team.RED)
                        GameManager.currentTurn = Team.BLUE
                    } else {
                        GameManager.blueTargetRegion = regionName
                        showFlag(regionName, Team.BLUE)
                        launchQuestionPhase()
                    }
                } else {
                    // --- ATTACK PHASE ---
                    GameManager.redTargetRegion = regionName
                    GameManager.blueTargetRegion = regionName
                    showFlag(regionName, currentPlayer)
                    isFirstAnswerer = true
                    startQuestionActivity()
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

        val currentlyAnsweringRed = if (GameManager.currentPhase == GamePhase.CAPTURE) {
            isRedTurnToAnswer // Standard Red -> Blue
        } else {
            // In Attack Phase: first is Attacker, second is Defender
            if (isFirstAnswerer) {
                GameManager.currentTurn == Team.RED
            } else {
                GameManager.currentTurn == Team.BLUE // If Blue is attacking, Red is defending (and vice versa)
            }
        }

        intent.putExtra("IS_RED_TURN", currentlyAnsweringRed)
        startActivityForResult(intent, 1001)
    }

    private fun launchDuelPhase() {
        val intent = Intent(this, TieBreakerActivity::class.java)
        startActivityForResult(intent, 1002) // 1002 for Duels
    }
    private fun showRoundBanner() {
        val roundLayout = findViewById<View>(R.id.roundBannerLayout)
        val roundText = findViewById<TextView>(R.id.roundNumberText)

        // Update the text to match the current round
        roundText.text = GameManager.currentRound.toString()
        roundLayout.bringToFront()
        roundLayout.z = 100f

        roundLayout.visibility = View.VISIBLE

        // Hide it automatically after 2 seconds
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            roundLayout.visibility = View.GONE
        }, 2000)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // ==========================================================
        // ⚔️ TIE-BREAKER DUEL RESULT (1002)
        // ==========================================================
        if (requestCode == 1002 && resultCode == Activity.RESULT_OK) {
            val winnerString = data?.getStringExtra("WINNING_TEAM")
            val winningTeam = if (winnerString == "RED") Team.RED else Team.BLUE

            GameManager.resolveDuel(winningTeam, GameManager.redTargetRegion!!)

            // Check if the map is now full (in case this duel was for the last neutral territory)
            val neutralExists = GameManager.territories.values.any { it.owner == Team.NEUTRAL }
            if (GameManager.currentPhase == GamePhase.ATTACK) {
                handleEndOfAttackTurn()
            }
            if (!neutralExists && GameManager.currentPhase == GamePhase.CAPTURE) {
                showPhaseBanner(GamePhase.ATTACK) {
                    GameManager.currentPhase = GamePhase.ATTACK
                    GameManager.currentTurn = Team.BLUE // BLUE STARTS THE ATTACK PHASE
                    isFirstAnswerer = true
                    updateMapVisuals()
                    Toast.makeText(this, "Фаза Атака: Ред на Сините!", Toast.LENGTH_SHORT).show()
                }
            } else {
                finishAttackRound()
            }
            return
        }

        // ==========================================================
        // 🧠 STANDARD QUESTION RESULT (1001)
        // ==========================================================
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            val wasCorrect = data?.getBooleanExtra("WAS_CORRECT", false) ?: false

            // --- CASE A: CAPTURE PHASE ---
            if (GameManager.currentPhase == GamePhase.CAPTURE) {
                if (isRedTurnToAnswer) {
                    // Red finished
                    GameManager.redLastResult = wasCorrect
                    isRedTurnToAnswer = false
                    startQuestionActivity()
                } else {
                    // Blue finished
                    GameManager.blueLastResult = wasCorrect
                    val phaseBeforeRound = GameManager.currentPhase

                    // Check if they clicked the exact same territory (The 1-Neutral Deadlock)
                    val foughtForSameTerritory = (GameManager.redTargetRegion == GameManager.blueTargetRegion && GameManager.redTargetRegion != null)

                    // DEADLOCK: Both Right -> Tie Breaker!
                    if (foughtForSameTerritory && GameManager.redLastResult && GameManager.blueLastResult) {
                        // Wait 1.5 seconds so they can breathe, then show the banner
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            showPhaseBanner(R.drawable.decided_teritory) { launchDuelPhase() }
                        }, 1500)
                        return
                    }

                    // NORMAL RESOLUTION (or someone was wrong in the Deadlock)
                    GameManager.resolveRound(GameManager.redLastResult, GameManager.blueLastResult)
                    updateMapVisuals()

                    val redOwnsAll = GameManager.territories.values.all { it.owner == Team.RED }
                    val blueOwnsAll = GameManager.territories.values.all { it.owner == Team.BLUE }

                    if (GameManager.instantWinner != null || redOwnsAll || blueOwnsAll) {
                        showWinnerScreen()
                        return
                    }

                    // Phase Transition Check
                    if (phaseBeforeRound == GamePhase.CAPTURE && GameManager.currentPhase == GamePhase.ATTACK) {
                        showPhaseBanner(GamePhase.ATTACK) {
                            GameManager.currentPhase = GamePhase.ATTACK
                            GameManager.currentTurn = Team.BLUE // BLUE STARTS ATTACK PHASE
                            isFirstAnswerer = true

                            // --- RESET TRACKERS AND SHOW ROUND 1 ---
                            GameManager.currentRound = 1
                            GameManager.attackTurnsCompleted = 0
                            showRoundBanner()

                            updateMapVisuals()
                            Toast.makeText(this, "Фаза Атака: Ред на Сините!", Toast.LENGTH_SHORT).show()
                        }
                    }
                    else {
                        // --- THE AUTO-SELECT FIX ---
                        val neutrals = GameManager.territories.values.filter { it.owner == Team.NEUTRAL }

                        if (neutrals.size == 1) {
                            // Exactly 1 territory left! Force the battle immediately!
                            val lastRegion = neutrals.first().name
                            GameManager.redTargetRegion = lastRegion
                            GameManager.blueTargetRegion = lastRegion

                            showFlag(lastRegion, Team.RED)
                            showFlag(lastRegion, Team.BLUE)

                            Toast.makeText(this, "Битка за последната територия!", Toast.LENGTH_LONG).show()

                            // Wait 2 seconds so they see the map update, then automatically launch!
                            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                                isRedTurnToAnswer = true
                                startQuestionActivity()
                            }, 2000)
                        } else {
                            // Normal turn continuation (2+ territories left)
                            GameManager.currentTurn = Team.RED
                            isRedTurnToAnswer = true
                        }
                    }

                    // Phase Transition Check
                    if (phaseBeforeRound == GamePhase.CAPTURE && GameManager.currentPhase == GamePhase.ATTACK) {
                        showPhaseBanner(GamePhase.ATTACK) {
                            GameManager.currentPhase = GamePhase.ATTACK
                            GameManager.currentTurn = Team.BLUE // BLUE STARTS
                            isFirstAnswerer = true
                            GameManager.currentRound = 1
                            showRoundBanner()
                            updateMapVisuals()
                            Toast.makeText(this, "Фаза Атака: Ред на Сините!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        GameManager.currentTurn = Team.RED
                        isRedTurnToAnswer = true
                    }
                }
            }
            // --- CASE B: ATTACK PHASE (Attacker-First Logic) ---
            else {
                val attacker = GameManager.currentTurn
                val defender = if (attacker == Team.RED) Team.BLUE else Team.RED

                if (isFirstAnswerer) {
                    // First person answering is ALWAYS the attacker
                    if (attacker == Team.RED) GameManager.redLastResult = wasCorrect
                    else GameManager.blueLastResult = wasCorrect

                    if (!wasCorrect) {
                        // ATTACKER FAILED: End round immediately
                        Toast.makeText(this, "Атаката се провали!", Toast.LENGTH_SHORT).show()
                        GameManager.resolveDuel(defender, GameManager.redTargetRegion!!)
                        handleEndOfAttackTurn()
                        isFirstAnswerer = true // Reset for next round
                        finishAttackRound()
                    } else {
                        // Attacker was correct: Now defender must answer
                        isFirstAnswerer = false
                        startQuestionActivity()
                    }
                } else {
                    // Second person answering is ALWAYS the defender
                    if (defender == Team.RED) GameManager.redLastResult = wasCorrect
                    else GameManager.blueLastResult = wasCorrect

                    // Both have now answered
                    if (GameManager.redLastResult && GameManager.blueLastResult) {
                        // Both correct -> Duel (Wait 1.5 seconds here too for consistency)
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            showPhaseBanner(R.drawable.decided_teritory) { launchDuelPhase() }
                        }, 1500)
                    } else {
                        // Attacker was correct (checked above), Defender was wrong -> Attacker wins
                        GameManager.resolveDuel(attacker, GameManager.redTargetRegion!!)

                        handleEndOfAttackTurn()
                        isFirstAnswerer = true // Reset
                        finishAttackRound()
                    }
                }
            }
        }
    }

    private fun finishAttackRound() {
        // Force the map to look at the new owners in GameManager
        updateMapVisuals()
        hideAllFlags()

        // Check Game Over
        val redOwnsAll = GameManager.territories.values.all { it.owner == Team.RED }
        val blueOwnsAll = GameManager.territories.values.all { it.owner == Team.BLUE }

        if (GameManager.instantWinner != null || redOwnsAll || blueOwnsAll) {
            val finalWinner = GameManager.instantWinner ?: if (redOwnsAll) Team.RED else Team.BLUE
            GameManager.territories.values.forEach { it.owner = finalWinner }
            updateMapVisuals()
            showWinnerScreen()
            return
        }

        // Switch Attacker Turn!
        GameManager.currentTurn = if (GameManager.currentTurn == Team.RED) Team.BLUE else Team.RED
        isRedTurnToAnswer = true
        Toast.makeText(this, "Атакуват ${if(GameManager.currentTurn == Team.RED) "Червените" else "Сините"}!", Toast.LENGTH_LONG).show()
    }

    private fun showPhaseBanner(data: Any?, onComplete: (() -> Unit)? = null) {
        val phaseOverlay = findViewById<View>(R.id.phaseOverlayLayout)
        val phaseImage = findViewById<ImageView>(R.id.phaseImage)

        // This handles both GamePhase and R.drawable IDs correctly
        when (data) {
            is GamePhase -> {
                if (data == GamePhase.CAPTURE) phaseImage.setImageResource(R.drawable.capture_phase)
                else phaseImage.setImageResource(R.drawable.attack_phase)
            }
            is Int -> phaseImage.setImageResource(data)
        }

        phaseOverlay.visibility = View.VISIBLE

        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            phaseOverlay.visibility = View.GONE
            // This is the critical part: it runs the logic ONLY after the banner is gone
            onComplete?.invoke()
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
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        findViewById<Button>(R.id.btnRematch).setOnClickListener {
            winnerOverlay.visibility = View.GONE
            GameManager.startNewGame()
            updateMapVisuals()
            Toast.makeText(this, "Нова игра започва!", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnQuit).setOnClickListener {
            finishAffinity()
        }
    }
}