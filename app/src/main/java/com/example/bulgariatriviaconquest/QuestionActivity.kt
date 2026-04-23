package com.example.bulgariatriviaconquest

import android.app.Activity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class QuestionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question)

        val regionName = intent.getStringExtra("REGION_NAME") ?: "Територия"
        findViewById<TextView>(R.id.tvRegionName).text = "Битка за $regionName"

        // For now, we use a placeholder question.
        // Soon we will pull a random one from a list.
        val questionText = "Коя година е основана Първата българска държава?"
        val options = listOf("632", "681", "864", "1018")
        val correctIndex = 1

        findViewById<TextView>(R.id.tvQuestionText).text = questionText

        val buttons = listOf(
            findViewById<Button>(R.id.btnAnswer1),
            findViewById<Button>(R.id.btnAnswer2),
            findViewById<Button>(R.id.btnAnswer3),
            findViewById<Button>(R.id.btnAnswer4)
        )

        buttons.forEachIndexed { index, button ->
            button.text = options[index]
            button.setOnClickListener {
                if (index == correctIndex) {
                    setResult(Activity.RESULT_OK) // Player won!
                } else {
                    setResult(Activity.RESULT_CANCELED) // Player lost
                }
                finish() // Go back to the map
            }
        }
    }
}