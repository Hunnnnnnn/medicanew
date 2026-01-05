package com.example.medica.Model

data class AppointmentData(
    val id: String = "",
    val userId: String = "",
    val patientName: String = "", // Name from Patient Details form
    val doctorId: String = "",
    val doctorName: String = "",
    val specialty: String = "",
    val date: String = "",
    val time: String = "",
    val status: String = "upcoming", // "upcoming", "completed", "cancelled"
    val createdAt: Long = System.currentTimeMillis(),
    val imageRes: Int = 0,
    val location: String = "Poli umum"
)