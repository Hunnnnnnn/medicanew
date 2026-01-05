package com.example.medica.Model


data class ProfileMenu(
    val title: String,
    val iconRes: Int,
    val endText: String = "",
    val isLogout: Boolean = false
)