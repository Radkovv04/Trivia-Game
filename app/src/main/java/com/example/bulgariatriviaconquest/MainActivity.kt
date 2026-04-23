package com.example.bulgariatriviaconquest

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var pendingRegion: String? = null // Add this at the top of MainActivity class
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // IMPORTANT: Start the game state!
        GameManager.startNewGame()
        updateMapVisuals()

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

                    // Using your specific hex codes from the previous message
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

                    if (regionName != null) {
                        handleTerritoryClick(regionName)
                    }
                }
            }
            true
        }
    }
    private fun updateMapVisuals() {
        for ((name, territory) in GameManager.territories) {
            val overlayId = when(name) {
                "Видин" -> R.id.overlayVidin
                "Плевен" -> R.id.overlayPleven // Make sure these IDs match what you named them in XML
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
                    overlayView.visibility = android.view.View.VISIBLE

                    // Define your custom light blue here
                    val customBlue = android.graphics.Color.parseColor("#1FAEFF")
                    val customRed = android.graphics.Color.parseColor("#E5123C")

                    val color = if (territory.owner == Team.RED) customRed else customBlue
                    overlayView.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN)

                    // Set transparency to 50% so you can still read the text underneath!
                    overlayView.alpha = 0.5f
                } else {
                    overlayView.visibility = android.view.View.INVISIBLE
                }
            }
        }
    }

    // Separate function to keep the logic clean
    private fun handleTerritoryClick(regionName: String) {
        pendingRegion = regionName // Remember the name before leaving the screen
        val intent = Intent(this, QuestionActivity::class.java)
        intent.putExtra("REGION_NAME", regionName)
        startActivityForResult(intent, 1001)
        val territory = GameManager.territories[regionName]
        val currentPlayer = GameManager.currentTurn

        // Adjacency check: Does any neighbor belong to the current player?
        val isConnected = territory?.adjacentTerritories?.any { adjName ->
            GameManager.territories[adjName]?.owner == currentPlayer
        } ?: false

        // SPECIAL RULE: If you click your own Kingdom or owned land, it shouldn't show the error.
        if (territory?.owner == currentPlayer) {
            Toast.makeText(this, "Това вече е ваше!", Toast.LENGTH_SHORT).show()
            return
        }

        if (isConnected && territory?.owner == Team.NEUTRAL) {
            // Launch Question
            val intent = Intent(this, QuestionActivity::class.java)
            intent.putExtra("REGION_NAME", regionName)
            startActivityForResult(intent, 1001)
        } else {
            Toast.makeText(this, "Можете да атакувате само съседни територии! Сега е ред на $currentPlayer", Toast.LENGTH_SHORT).show()
        }
    }

    // This runs when you return from the QuestionActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            pendingRegion?.let { regionName ->
                val territory = GameManager.territories[regionName]
                territory?.owner = GameManager.currentTurn // Capture it!

                Toast.makeText(this, "$regionName вече е за ${GameManager.currentTurn}!", Toast.LENGTH_SHORT).show()

                // SWITCH TURNS
                GameManager.currentTurn = if (GameManager.currentTurn == Team.RED) Team.BLUE else Team.RED
                updateMapVisuals()
            }
        }
    }
}