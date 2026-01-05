package com.example.medica.Model

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class EmailAuthClient {
    
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    
    /**
     * Sign up dengan email & password
     */
    fun signUp(
        email: String,
        password: String,
        onSuccess: (FirebaseUser) -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result.user?.let { user ->
                        onSuccess(user)
                    } ?: onFailure("User data tidak ditemukan")
                } else {
                    onFailure(task.exception?.message ?: "Sign up gagal")
                }
            }
    }
    
    /**
     * Sign in dengan email & password
     */
    fun signIn(
        email: String,
        password: String,
        onSuccess: (FirebaseUser) -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    task.result.user?.let { user ->
                        onSuccess(user)
                    } ?: onFailure("User data tidak ditemukan")
                } else {
                    onFailure(task.exception?.message ?: "Login gagal")
                }
            }
    }
    
    /**
     * Get current user
     */
    fun getCurrentUser(): FirebaseUser? = auth.currentUser
    
    /**
     * Sign out
     */
    fun signOut() = auth.signOut()
    
    /**
     * Reset password via email
     */
    fun sendPasswordResetEmail(
        email: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure(task.exception?.message ?: "Gagal mengirim email reset")
                }
            }
    }
}
