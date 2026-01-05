package com.example.medica.Model

data class UserData(
    val id: String = "",
    val name: String = "",
    val nik: String = "",
    val email: String = "",
    val dob: String = "",
    val gender: String = "",
    val phone: String = "",
    val favoriteDoctors: List<String> = emptyList()
)