package com.example.medica.Repository

import com.example.medica.Model.UserData
import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {
    private val db = FirebaseFirestore.getInstance()

    fun saveUserToFirestore(
        userData: UserData,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        // Simpan data ke collection "users"
        db.collection("users").document(userData.id)
            .set(userData)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Gagal menyimpan data user")
            }
    }
    
    /**
     * Get user data from Firestore by userId
     */
    fun getUserData(
        userId: String,
        onSuccess: (UserData) -> Unit,
        onFailure: (String) -> Unit
    ) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userData = document.toObject(UserData::class.java)
                    if (userData != null) {
                        onSuccess(userData)
                    } else {
                        onFailure("Failed to parse user data")
                    }
                } else {
                    onFailure("User not found")
                }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Failed to get user data")
            }
    }
}