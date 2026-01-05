package com.example.medica.Repository

import com.example.medica.Model.AppointmentData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class AppointmentRepository {
    private val db = FirebaseFirestore.getInstance()
    private val appointmentsCollection = db.collection("appointments")

    /**
     * Create new appointment
     */
    fun createAppointment(
        appointment: AppointmentData,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        appointmentsCollection
            .add(appointment)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Gagal membuat appointment")
            }
    }

    /**
     * Get all appointments for a user
     */
    fun getUserAppointments(
        userId: String,
        onSuccess: (List<AppointmentData>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        appointmentsCollection
            .whereEqualTo("userId", userId)
            // Temporarily removed orderBy to avoid index requirement
            // .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                android.util.Log.d("AppointmentRepo", "Found ${documents.size()} appointments for user $userId")
                val appointments = documents.mapNotNull { document ->
                    val appointment = document.toObject(AppointmentData::class.java).copy(
                        id = document.id
                    )
                    android.util.Log.d("AppointmentRepo", "Appointment: ${appointment.doctorName}, status: ${appointment.status}")
                    appointment
                }
                onSuccess(appointments)
            }
            .addOnFailureListener { e ->
                android.util.Log.e("AppointmentRepo", "Error loading appointments", e)
                onFailure(e.message ?: "Gagal mengambil appointments")
            }
    }

    /**
     * Get appointments by status (upcoming, completed, cancelled)
     */
    fun getUserAppointmentsByStatus(
        userId: String,
        status: String,
        onSuccess: (List<AppointmentData>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        appointmentsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("status", status)
            .orderBy("date", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val appointments = documents.mapNotNull { document ->
                    document.toObject(AppointmentData::class.java).copy(
                        id = document.id
                    )
                }
                onSuccess(appointments)
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Gagal mengambil appointments")
            }
    }

    /**
     * Update appointment status
     */
    fun updateAppointmentStatus(
        appointmentId: String,
        status: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        appointmentsCollection.document(appointmentId)
            .update("status", status)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Gagal update status")
            }
    }

    /**
     * Reschedule appointment
     */
    fun rescheduleAppointment(
        appointmentId: String,
        newDate: String,
        newTime: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val updates = hashMapOf<String, Any>(
            "date" to newDate,
            "time" to newTime
        )

        appointmentsCollection.document(appointmentId)
            .update(updates)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Gagal reschedule appointment")
            }
    }

    /**
     * Cancel appointment
     */
    fun cancelAppointment(
        appointmentId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        updateAppointmentStatus(appointmentId, "cancelled", onSuccess, onFailure)
    }

    /**
     * Delete appointment
     */
    fun deleteAppointment(
        appointmentId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        appointmentsCollection.document(appointmentId)
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Gagal menghapus appointment")
            }
    }
}
