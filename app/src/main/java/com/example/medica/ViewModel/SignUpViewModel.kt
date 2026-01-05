package com.example.medica.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medica.Model.PhoneAuthClient
import com.example.medica.Model.UserData
import com.example.medica.Repository.RegistrationCache
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SignUpUiState(
    val name: String = "",
    val nik: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val dob: String = "",
    val gender: String = "",
    val phone: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class SignUpViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun onNikChange(nik: String) {
        // Hanya terima angka dan maksimal 16 digit
        if (nik.all { it.isDigit() } && nik.length <= 16) {
            _uiState.update { it.copy(nik = nik) }
        }
    }

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email) }
    }

    fun onDobChange(dob: String) {
        _uiState.update { it.copy(dob = dob) }
    }

    fun onGenderChange(gender: String) {
        _uiState.update { it.copy(gender = gender) }
    }

    fun onPhoneChange(phone: String) {
        // Hanya terima angka
        if (phone.all { it.isDigit() }) {
            _uiState.update { it.copy(phone = phone) }
        }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password) }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    /**
     * Fungsi untuk register dan kirim OTP
     * Flow:
     * 1. Validasi semua field
     * 2. Simpan data sementara ke RegistrationCache
     * 3. Kirim OTP via PhoneAuthClient
     * 4. Callback ke UI dengan verificationId atau auto-verify
     */
    fun registerAndSendOtp(
        client: PhoneAuthClient,
        onOtpSent: (String) -> Unit,
        onAutoVerified: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            // Validasi input
            val validationError = validateInput()
            if (validationError != null) {
                _uiState.update { it.copy(errorMessage = validationError) }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Simpan data sementara (akan disimpan ke Firestore setelah verifikasi OTP)
                val userData = UserData(
                    id = "", // ID akan di-set setelah Firebase Auth berhasil
                    name = _uiState.value.name,
                    nik = _uiState.value.nik,
                    email = _uiState.value.email,
                    dob = _uiState.value.dob,
                    gender = _uiState.value.gender,
                    phone = _uiState.value.phone
                )
                RegistrationCache.tempData = userData

                // Format nomor telepon untuk Firebase (+62xxx)
                val fullPhoneNumber = "+62${_uiState.value.phone}"

                // Kirim OTP
                client.sendOtp(
                    phoneNumber = fullPhoneNumber,
                    onCodeSent = { verificationId ->
                        _uiState.update { it.copy(isLoading = false) }
                        onOtpSent(verificationId)
                    },
                    onVerificationFailed = { exception ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Gagal mengirim OTP: ${exception.message}"
                            )
                        }
                    },
                    onAutoVerified = {
                        _uiState.update { it.copy(isLoading = false) }
                        onAutoVerified?.invoke()
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Terjadi kesalahan: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Register dengan Email/Password (tanpa OTP)
     * Flow:
     * 1. Validasi semua field
     * 2. Cek password match
     * 3. Create user di Firebase Auth
     * 4. Simpan data ke Firestore
     */
    fun registerWithEmail(
        emailAuthClient: com.example.medica.Model.EmailAuthClient,
        userRepository: com.example.medica.Repository.UserRepository,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            // Validasi input
            val validationError = validateInputWithPassword()
            if (validationError != null) {
                _uiState.update { it.copy(errorMessage = validationError) }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                // Create user dengan email/password
                emailAuthClient.signUp(
                    email = _uiState.value.email,
                    password = _uiState.value.password,
                    onSuccess = { firebaseUser ->
                        // Simpan data user ke Firestore
                        val userData = UserData(
                            id = firebaseUser.uid,
                            name = _uiState.value.name,
                            nik = _uiState.value.nik,
                            email = _uiState.value.email,
                            dob = _uiState.value.dob,
                            gender = _uiState.value.gender,
                            phone = _uiState.value.phone
                        )

                        userRepository.saveUserToFirestore(
                            userData = userData,
                            onSuccess = {
                                _uiState.update { it.copy(isLoading = false) }
                                onSuccess()
                            },
                            onFailure = { errorMsg ->
                                _uiState.update {
                                    it.copy(
                                        isLoading = false,
                                        errorMessage = "Gagal menyimpan data: $errorMsg"
                                    )
                                }
                            }
                        )
                    },
                    onFailure = { errorMsg ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = errorMsg
                            )
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Terjadi kesalahan: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Validasi semua input field
     * Return: error message jika ada yang tidak valid, null jika semua valid
     */
    private fun validateInput(): String? {
        val state = _uiState.value

        return when {
            state.name.isBlank() -> "Nama lengkap harus diisi"
            state.name.length < 3 -> "Nama minimal 3 karakter"
            
            state.nik.isBlank() -> "NIK harus diisi"
            state.nik.length != 16 -> "NIK harus 16 digit"
            
            state.email.isBlank() -> "Email harus diisi"
            !isValidEmail(state.email) -> "Format email tidak valid"
            
            state.dob.isBlank() -> "Tanggal lahir harus diisi"
            
            state.gender.isBlank() -> "Gender harus dipilih"
            
            state.phone.isBlank() -> "Nomor telepon harus diisi"
            state.phone.length < 10 -> "Nomor telepon minimal 10 digit"
            state.phone.length > 13 -> "Nomor telepon maksimal 13 digit"
            !state.phone.startsWith("8") -> "Nomor telepon harus diawali dengan 8 (contoh: 81234567890)"
            
            else -> null
        }
    }

    /**
     * Validasi format email
     */
    private fun isValidEmail(email: String): Boolean {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return email.matches(emailRegex)
    }

    /**
     * Validasi input dengan password (untuk email/password signup)
     */
    private fun validateInputWithPassword(): String? {
        val state = _uiState.value

        return when {
            state.name.isBlank() -> "Nama lengkap harus diisi"
            state.name.length < 3 -> "Nama minimal 3 karakter"
            
            state.nik.isBlank() -> "NIK harus diisi"
            state.nik.length != 16 -> "NIK harus 16 digit"
            
            state.email.isBlank() -> "Email harus diisi"
            !isValidEmail(state.email) -> "Format email tidak valid"
            
            state.password.isBlank() -> "Password harus diisi"
            state.password.length < 6 -> "Password minimal 6 karakter"
            
            state.confirmPassword.isBlank() -> "Konfirmasi password harus diisi"
            state.password != state.confirmPassword -> "Password tidak cocok"
            
            state.dob.isBlank() -> "Tanggal lahir harus diisi"
            
            state.gender.isBlank() -> "Gender harus dipilih"
            
            state.phone.isBlank() -> "Nomor telepon harus diisi"
            state.phone.length < 10 -> "Nomor telepon minimal 10 digit"
            state.phone.length > 13 -> "Nomor telepon maksimal 13 digit"
            !state.phone.startsWith("8") -> "Nomor telepon harus diawali dengan 8 (contoh: 81234567890)"
            
            else -> null
        }
    }
}