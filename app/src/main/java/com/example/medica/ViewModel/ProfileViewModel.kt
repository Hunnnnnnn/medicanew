package com.example.medica.ViewModel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medica.Model.ProfileMenu
import com.example.medica.R
import com.example.medica.Repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel : ViewModel() {
    
    private val userRepository = UserRepository()
    private val auth = FirebaseAuth.getInstance()
    
    private val _userName = MutableStateFlow("Loading...")
    val userName = _userName.asStateFlow()
    
    private val _userPhone = MutableStateFlow("")
    val userPhone = _userPhone.asStateFlow()
    
    private val _selectedLanguage = MutableStateFlow("English")
    val selectedLanguage = _selectedLanguage.asStateFlow()
    
    private val _languageChanged = MutableStateFlow(false)
    val languageChanged = _languageChanged.asStateFlow()
    
    private val _menuList = MutableStateFlow<List<ProfileMenu>>(emptyList())
    val menuList = _menuList.asStateFlow()

    init {
        loadUserData()
        loadMenus()
    }
    
    /**
     * Initialize with context for SharedPreferences
     */
    fun initialize(context: Context) {
        val prefs = context.getSharedPreferences("medica_prefs", Context.MODE_PRIVATE)
        _selectedLanguage.value = prefs.getString("app_language", "English") ?: "English"
        loadMenus()
    }
    
    /**
     * Load user data from Firebase
     */
    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _userName.value = "Guest"
            _userPhone.value = "Not logged in"
            return
        }
        
        viewModelScope.launch {
            userRepository.getUserData(
                userId = currentUser.uid,
                onSuccess = { userData ->
                    _userName.value = userData.name.ifEmpty { "User" }
                    _userPhone.value = userData.phone.ifEmpty { currentUser.phoneNumber ?: "" }
                },
                onFailure = { error ->
                    android.util.Log.e("ProfileVM", "Error loading user: $error")
                    // Fallback to auth data
                    _userName.value = currentUser.displayName ?: "User"
                    _userPhone.value = currentUser.phoneNumber ?: ""
                }
            )
        }
    }
    
    /**
     * Update profile data
     */
    fun updateProfile(newName: String, newPhone: String) {
        _userName.value = newName
        _userPhone.value = newPhone
    }
    
    /**
     * Save language preference and trigger restart
     */
    fun setLanguage(context: Context, language: String) {
        if (_selectedLanguage.value != language) {
            _selectedLanguage.value = language
            val prefs = context.getSharedPreferences("medica_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("app_language", language).apply()
            loadMenus() // Reload menus to update language text
            _languageChanged.value = true // Signal that app needs restart
        }
    }
    
    /**
     * Reset language changed flag
     */
    fun resetLanguageChanged() {
        _languageChanged.value = false
    }

    /**
     * Load menu items
     */
    private fun loadMenus() {
        val menus = listOf(
            ProfileMenu(title = "Edit Profile", iconRes = R.drawable.ic_profile),
            ProfileMenu(title = "Notification", iconRes = R.drawable.ic_notifications),
            ProfileMenu(title = "Security", iconRes = R.drawable.ic_security),
            ProfileMenu(title = "Language", iconRes = R.drawable.ic_language, endText = _selectedLanguage.value),
            ProfileMenu(title = "Logout", iconRes = R.drawable.ic_logout, isLogout = true)
        )
        _menuList.value = menus
    }
    
    /**
     * Reload user data (call after updating profile)
     */
    fun reloadUserData() {
        loadUserData()
    }
}