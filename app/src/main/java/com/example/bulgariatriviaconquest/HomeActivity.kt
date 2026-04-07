package com.example.bulgariatriviaconquest

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

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