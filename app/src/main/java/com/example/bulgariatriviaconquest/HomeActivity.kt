package com.example.bulgariatriviaconquest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.w3c.dom.Text
import kotlin.toString

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        findViewById<Button>(R.id.btnLeaderboards).setOnClickListener {
            Toast.makeText(this, "Очаквайте скоро!", Toast.LENGTH_SHORT).show()
        }

        // Find the invisible Play button and tell it to open the Map
        findViewById<Button>(R.id.btnPlay).setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // The Quit button closes the app
        findViewById<Button>(R.id.btnQuit).setOnClickListener {
            finishAffinity()
        }
    }
}