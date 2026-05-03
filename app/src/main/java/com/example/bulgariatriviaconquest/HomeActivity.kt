package com.example.bulgariatriviaconquest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import org.w3c.dom.Text
import kotlin.toString

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        findViewById<Button>(R.id.btnLeaderboards).setOnClickListener {
            Toast.makeText(this, "Очаквайте скоро!", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.btnPlay).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        findViewById<Button>(R.id.btnQuit).setOnClickListener {
            finishAffinity()
        }
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
        val switchDifficulty = findViewById<androidx.appcompat.widget.SwitchCompat>(R.id.switchDifficulty)

        btnSettings.setOnClickListener { settingsOverlay.visibility = View.VISIBLE }
        btnCloseSettings.setOnClickListener { settingsOverlay.visibility = View.GONE }

        settingsOverlay.setOnClickListener { settingsOverlay.visibility = View.GONE }

        switchAudio.isChecked = GameManager.isAudioEnabled
        switchAudio.text = if (GameManager.isAudioEnabled) "ЗВУК: ВКЛЮЧЕН" else "ЗВУК: ИЗКЛЮЧЕН"

        switchDifficulty.isChecked = GameManager.isHardMode
        switchDifficulty.text = if (GameManager.isHardMode) "ТРУДНОСТ: ТРУДНА" else "ТРУДНОСТ: ЛЕСНА"

        switchAudio.setOnCheckedChangeListener { _, isChecked ->
            GameManager.isAudioEnabled = isChecked
            switchAudio.text = if (isChecked) "ЗВУК: ВКЛЮЧЕН" else "ЗВУК: ИЗКЛЮЧЕН"
        }
        switchGraphics.setOnCheckedChangeListener { _, isChecked ->
            switchGraphics.text = if (isChecked) "ГРАФИКА: ВИСОКА" else "ГРАФИКА: НИСКА"
        }

        switchDifficulty.setOnCheckedChangeListener { _, isChecked ->
            GameManager.isHardMode = isChecked
            switchDifficulty.text = if (isChecked) "ТРУДНОСТ: ТРУДНА" else "ТРУДНОСТ: ЛЕСНА"
        }
        val topUsernameText = findViewById<TextView>(R.id.topUsernameText)
        val btnLogout = findViewById<View>(R.id.btnLogout)
        val tvVersion = findViewById<TextView>(R.id.tvVersion)
        tvVersion.setOnClickListener {
            Toast.makeText(this, "Нова версия очаквайте скоро!", Toast.LENGTH_SHORT).show()
        }

        topUsernameText.text = GameManager.playerUsername
        btnLogout.setOnClickListener {
            com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
            GameManager.playerUsername = "Играч"
            android.widget.Toast.makeText(this, "Успешно излязохте от профила си", android.widget.Toast.LENGTH_SHORT).show()
            val intent = android.content.Intent(this, LoginActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}