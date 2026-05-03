package com.example.bulgariatriviaconquest

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://bulgaria-trivia-default-rtdb.firebaseio.com/").reference
        val loginLayout = findViewById<View>(R.id.loginLayout)
        val registerLayout = findViewById<View>(R.id.registerLayout)
        val loginEmail = findViewById<EditText>(R.id.loginEmail)
        val loginPassword = findViewById<EditText>(R.id.loginPassword)
        val btnLoginSubmit = findViewById<View>(R.id.btnLoginSubmit)
        val btnGoToRegister = findViewById<View>(R.id.btnGoToRegister)
        val regEmail = findViewById<EditText>(R.id.regEmail)
        val regUsername = findViewById<EditText>(R.id.regUsername)
        val regPassword = findViewById<EditText>(R.id.regPassword)
        val btnRegisterSubmit = findViewById<View>(R.id.btnRegisterSubmit)
        val btnBackToLogin = findViewById<View>(R.id.btnBackToLogin)

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val loginLayout = findViewById<View>(R.id.loginLayout)
                val registerLayout = findViewById<View>(R.id.registerLayout)
                if (registerLayout.visibility == View.VISIBLE) {
                    registerLayout.visibility = View.GONE
                    loginLayout.visibility = View.VISIBLE
                } else {
                    finishAffinity()
                }
            }
        })
        btnGoToRegister.setOnClickListener {
            loginLayout.visibility = View.GONE
            registerLayout.visibility = View.VISIBLE
        }
        btnBackToLogin.setOnClickListener {
            registerLayout.visibility = View.GONE
            loginLayout.visibility = View.VISIBLE
        }
        btnRegisterSubmit.setOnClickListener {
            val email = regEmail.text.toString().trim()
            val username = regUsername.text.toString().trim()
            val password = regPassword.text.toString().trim()
            if (email.isEmpty() || username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Моля, попълнете всички полета!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (username.length > 16) {
                Toast.makeText(this, "Името трябва да е до 16 символа!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.length < 6) {
                Toast.makeText(this, "Паролата трябва да е поне 6 символа!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val forbiddenSymbols = listOf(".", "#", "$", "[", "]", "/", "|", "-") // Symbol cheker
            for (symbol in forbiddenSymbols) {
                if (username.contains(symbol)) {
                    Toast.makeText(this, "Името не може да съдържа: . # $ [ ] / | -", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
            }
            database.child("usernames").child(username).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    Toast.makeText(this, "Това име вече е заето! Избери друго.", Toast.LENGTH_LONG).show()
                } else {
                    // Create User
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                                database.child("usernames").child(username).setValue(userId)
                                database.child("users").child(userId).child("username").setValue(username)
                                GameManager.playerUsername = username
                                Toast.makeText(this, "Регистрацията е успешна!", Toast.LENGTH_SHORT).show()
                                goToMainMenu()
                            } else {
                                Toast.makeText(this, "Firebase грешка: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                }
            }.addOnFailureListener { e ->
                val regUsername = findViewById<EditText>(R.id.regUsername)
                regUsername.setText("ГРЕШКА: ${e.message}")
            }
        }
        btnLoginSubmit.setOnClickListener {
            val email = loginEmail.text.toString().trim()
            val password = loginPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Въведете имейл и парола!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                        database.child("users").child(userId).child("username").get().addOnSuccessListener { snapshot ->
                            val username = snapshot.value?.toString() ?: "Играч"
                            GameManager.playerUsername = username
                            Toast.makeText(this, "Добре дошъл, $username!", Toast.LENGTH_SHORT).show()
                            goToMainMenu()
                        }
                    } else {
                        Toast.makeText(this, "Грешен имейл или парола!", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
    //Auto logger
    override fun onStart() {
        super.onStart()
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            val database = FirebaseDatabase.getInstance("https://bulgaria-trivia-default-rtdb.firebaseio.com/").reference
            database.child("users").child(currentUser.uid).child("username").get().addOnSuccessListener { snapshot ->
                GameManager.playerUsername = snapshot.value?.toString() ?: "Играч"
                goToMainMenu()
            }
        }
    }
    private fun goToMainMenu() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
}