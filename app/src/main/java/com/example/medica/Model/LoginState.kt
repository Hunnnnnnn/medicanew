package com.example.medica.Model

data class LoginState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val verificationId: String? = null,
    val timer:Int = 0
)