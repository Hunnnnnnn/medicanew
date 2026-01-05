package com.example.medica.Model

import com.google.firebase.Timestamp

/**
 * Notification data model for Firebase Firestore
 * Matches TypeScript Notification interface from admin dashboard
 */
data class NotificationData(
    var id: String = "",
    var userId: String = "",
    var appointmentId: String = "",
    var type: String = "",              // "cancelled" or "rescheduled"
    var title: String = "",             // "Janji Temu Dibatalkan!" or "Jadwal Berubah"
    var message: String = "",           // Detailed notification message
    var timestamp: Timestamp? = null,
    var isRead: Boolean = false,
    var doctorName: String = "",
    var originalDate: String = "",
    var newDate: String = ""            // Empty for cancelled, has value for rescheduled
)