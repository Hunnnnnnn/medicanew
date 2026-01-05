package com.example.medica.Model

import android.app.Activity
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class PhoneAuthClient(private val activity: Activity) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // --- FUNGSI SEND OTP (UPDATED WITH AUTO-VERIFICATION) ---
    fun sendOtp(
        phoneNumber: String,
        onCodeSent: (String) -> Unit,            // Callback sukses kirim
        onVerificationFailed: (Exception) -> Unit, // Callback gagal
        onAutoVerified: (() -> Unit)? = null     // Callback auto-verification
    ) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d("PhoneAuth", "SMS Terkirim: $verificationId")
                // Panggil callback lambda
                onCodeSent(verificationId)
            }

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d("PhoneAuth", "Verifikasi Otomatis Berhasil")
                // PERBAIKAN: Sign in otomatis saat Firebase auto-detect SMS
                auth.signInWithCredential(credential)
                    .addOnCompleteListener(activity) { task ->
                        if (task.isSuccessful) {
                            Log.d("PhoneAuth", "Auto sign-in berhasil")
                            onAutoVerified?.invoke()
                        } else {
                            Log.e("PhoneAuth", "Auto sign-in gagal: ${task.exception?.message}")
                            // Jika auto sign-in gagal, tetap bisa manual verify
                        }
                    }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e("PhoneAuth", "Gagal: ${e.message}")
                // Panggil callback error
                onVerificationFailed(e)
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // --- FUNGSI VERIFIKASI KODE (DIPERBARUI) ---
    // Digunakan di OtpScreen / OtpViewModel
    fun verifyOtp(
        verificationId: String,
        code: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // Membuat credential dari ID dan Kode OTP
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithPhoneAuthCredential(credential, onSuccess, onError)
    }

    private fun signInWithPhoneAuthCredential(
        credential: PhoneAuthCredential,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onError(task.exception?.message ?: "Verifikasi Gagal")
                }
            }
    }
}