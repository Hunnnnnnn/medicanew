package com.example.medica.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medica.Model.GoogleAuthClient
import com.example.medica.Model.LoginState
import com.example.medica.Model.PhoneAuthClient
import com.example.medica.Repository.RegistrationCache
import com.example.medica.Repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginState())
    val uiState = _uiState.asStateFlow()

    private val userRepository = UserRepository()
    private val auth = FirebaseAuth.getInstance()

    fun checkAndSaveUser(
        onSuccess: () -> Unit,
        onFailure: ((String) -> Unit)? = null
    ) {
        val currentUser = auth.currentUser
        val cachedData = RegistrationCache.tempData

        if (currentUser != null) {
            if (cachedData != null) {
                val finalUserData = cachedData.copy(id = currentUser.uid)
                _uiState.update { it.copy(isLoading = true) }

                userRepository.saveUserToFirestore(
                    userData = finalUserData,
                    onSuccess = {
                        _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                        RegistrationCache.tempData = null
                        onSuccess()
                    },
                    onFailure = { errorMsg ->
                        _uiState.update { it.copy(isLoading = false, errorMessage = errorMsg) }
                        onFailure?.invoke(errorMsg)
                    }
                )
            } else {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                onSuccess()
            }
        } else {
            _uiState.update { it.copy(isLoading = false, errorMessage = "User tidak terautentikasi") }
            onFailure?.invoke("User tidak terautentikasi")
        }
    }

    fun signInWithGoogle(client: GoogleAuthClient, onSuccess: () -> Unit) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val success = client.signIn()
            if (success) {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                onSuccess()
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Login Google Gagal") }
            }
        }
    }

    fun sendOtp(
        phoneNumber: String,
        client: PhoneAuthClient,
        onCodeSent: (String) -> Unit,
        onAutoVerified: (() -> Unit)? = null
    ) {
        if (phoneNumber.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Nomor HP tidak boleh kosong") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        val fullNumber = if (phoneNumber.startsWith("+62")) phoneNumber else "+62$phoneNumber"

        client.sendOtp(
            phoneNumber = fullNumber,
            onCodeSent = { verificationId ->
                _uiState.update { it.copy(isLoading = false, verificationId = verificationId) }
                startResendTimer(60)
                onCodeSent(verificationId)
            },
            onVerificationFailed = { exception ->
                _uiState.update { it.copy(isLoading = false, errorMessage = exception.message) }
            },
            onAutoVerified = {
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                onAutoVerified?.invoke()
            }
        )
    }

    fun signInWithEmail(
        email: String,
        password: String,
        emailAuthClient: com.example.medica.Model.EmailAuthClient,
        onSuccess: () -> Unit
    ) {
        if (email.isEmpty() || password.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Email dan password harus diisi") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        emailAuthClient.signIn(
            email = email,
            password = password,
            onSuccess = { user ->
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
                onSuccess()
            },
            onFailure = { error ->
                _uiState.update { it.copy(isLoading = false, errorMessage = error) }
            }
        )
    }

    fun startResendTimer(seconds: Int = 60) {
        _uiState.update { it.copy(timer = seconds) }
        viewModelScope.launch {
            while (_uiState.value.timer > 0) {
                delay(1000L)
                _uiState.update { it.copy(timer = it.timer - 1) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}