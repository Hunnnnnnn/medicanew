package com.example.medica.Model

data class DoctorDetailData(
    var id: String = "",
    var name: String = "",
    var specialty: String = "",
    var hospital: String = "",
    var rating: Double = 0.0,
    var patientsCount: Int = 0,
    var yearsExperience: Int = 0,
    var imageUrl: String = "",
    var isAvailable: Boolean = true,
    var workingTime: String = "" // Changed from Int to String to match Firestore
)
