package com.example.medica.Config

/**
 * Feature flags untuk aplikasi
 * Toggle fitur on/off tanpa hapus kode
 */
object FeatureFlags {
    
    /**
     * Enable Phone OTP Authentication
     * 
     * Set ke TRUE jika sudah punya Firebase Blaze Plan
     * Set ke FALSE untuk pakai Email/Password only
     * 
     * Default: FALSE (Email/Password)
     */
    const val ENABLE_PHONE_OTP = false
    
    /**
     * Show Phone number field in signup
     * Even with email auth, we can still collect phone number
     */
    const val COLLECT_PHONE_NUMBER = true
    
    /**
     * Enable Google Sign-In
     */
    const val ENABLE_GOOGLE_SIGNIN = true
}
