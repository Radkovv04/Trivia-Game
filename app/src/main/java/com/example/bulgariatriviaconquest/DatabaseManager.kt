package com.example.bulgariatriviaconquest

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object DatabaseManager {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun createUserProfile(username: String, email: String, onResult: (Boolean) -> Unit) {
        val uid = auth.currentUser?.uid ?: return
        val profile = UserProfile(uid, username, email)

        db.collection("users").document(uid)
            .set(profile)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { onResult(false) }
    }

    fun getUserProfile(onResult: (UserProfile?) -> Unit) {
        val uid = auth.currentUser?.uid ?: return onResult(null)

        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                val profile = document.toObject(UserProfile::class.java)
                onResult(profile)
            }
            .addOnFailureListener { onResult(null) }
    }
}