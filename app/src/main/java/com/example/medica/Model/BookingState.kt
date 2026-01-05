package com.example.medica.Model

data class BookingState(
    val selectedDate: Int = 16, // Default tanggal 16
    val selectedTime: String = "10.00", // Default jam 10.00
    val currentMonth: String = "December 2025"
)