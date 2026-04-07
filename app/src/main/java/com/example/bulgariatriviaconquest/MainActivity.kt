package com.example.bulgariatriviaconquest

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Find our invisible province buttons
        val sofia = findViewById<Button>(R.id.btnSofia)
        val varna = findViewById<Button>(R.id.btnVarna)

        sofia.setOnClickListener {
            askQuestion("София", "Коя планина е до София?", "Витоша")
        }

        varna.setOnClickListener {
            askQuestion("Варна", "Как се казва морето до Варна?", "Черно море")
        }
    }

    private fun askQuestion(region: String, question: String, correctAnswer: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Битка за $region")
        builder.setMessage(question)

        builder.setPositiveButton(correctAnswer) { _, _ ->
            Toast.makeText(this, "Вярно! Превзехте $region!", Toast.LENGTH_LONG).show()
        }

        builder.setNegativeButton("Грешен отговор") { _, _ ->
            Toast.makeText(this, "Грешка! Армията ви се оттегля.", Toast.LENGTH_LONG).show()
        }

        builder.show()
    }
}