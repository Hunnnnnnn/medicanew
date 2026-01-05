package com.example.medica.Repository

import android.util.Log
import com.example.medica.Model.DoctorDetailData
import com.google.firebase.firestore.FirebaseFirestore

class DoctorRepository {
    private val db = FirebaseFirestore.getInstance()
    fun getDoctors(
        onSuccess: (List<DoctorDetailData>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        db.collection("doctors")
            .get()
            .addOnSuccessListener { result ->
                val doctorList = ArrayList<DoctorDetailData>()

                for (document in result) {
                    try {
                        val doctor = document.toObject(DoctorDetailData::class.java).copy(id = document.id)
                        doctorList.add(doctor)
                    } catch (e: Exception) {
                        Log.e("DoctorRepo", "Error parsing doctor: ${e.message}")
                    }
                }

                onSuccess(doctorList)
            }
            .addOnFailureListener { exception ->
                onFailure(exception.message ?: "Gagal mengambil data dokter")
            }
    }
    fun getDoctorById(
        doctorId: String,
        onSuccess: (DoctorDetailData?) -> Unit,
        onFailure: (String) -> Unit
    ) {
        Log.d("DoctorRepo", "getDoctorById called with ID: $doctorId")
        
        db.collection("doctors").document(doctorId)
            .get()
            .addOnSuccessListener { document ->
                Log.d("DoctorRepo", "Document exists: ${document.exists()}")
                if (document != null && document.exists()) {
                    try {
                        val doctor = document.toObject(DoctorDetailData::class.java)?.copy(id = document.id)
                        Log.d("DoctorRepo", "Doctor parsed: ${doctor?.name}, Rating: ${doctor?.rating}, Patients: ${doctor?.patientsCount}")
                        onSuccess(doctor)
                    } catch (e: Exception) {
                        Log.e("DoctorRepo", "Error parsing document: ${e.message}", e)
                        onFailure("Error parsing document: ${e.message}")
                    }
                } else {
                    Log.w("DoctorRepo", "Document does not exist for ID: $doctorId")
                    onSuccess(null)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("DoctorRepo", "Firestore query failed: ${exception.message}", exception)
                onFailure(exception.message ?: "Gagal mengambil detail dokter")
            }
    }

    /**
     * Get doctors by specialty
     */
    fun getDoctorsBySpecialty(
        specialty: String,
        onSuccess: (List<DoctorDetailData>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        db.collection("doctors")
            .whereEqualTo("specialty", specialty)
            .get()
            .addOnSuccessListener { result ->
                val doctorList = result.mapNotNull { document ->
                    try {
                        document.toObject(DoctorDetailData::class.java).copy(id = document.id)
                    } catch (e: Exception) {
                        Log.e("DoctorRepo", "Error parsing doctor: ${e.message}")
                        null
                    }
                }
                onSuccess(doctorList)
            }
            .addOnFailureListener { exception ->
                onFailure(exception.message ?: "Gagal mengambil dokter by specialty")
            }
    }

    /**
     * Toggle favorite doctor for a user
     */
    fun toggleFavoriteDoctor(
        userId: String,
        doctorId: String,
        isFavorite: Boolean,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val userRef = db.collection("users").document(userId)
        
        if (isFavorite) {
            // Add to favorites
            userRef.update("favoriteDoctors", com.google.firebase.firestore.FieldValue.arrayUnion(doctorId))
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onFailure(e.message ?: "Gagal menambah favorit") }
        } else {
            // Remove from favorites
            userRef.update("favoriteDoctors", com.google.firebase.firestore.FieldValue.arrayRemove(doctorId))
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { e -> onFailure(e.message ?: "Gagal menghapus favorit") }
        }
    }

    /**
     * Get favorite doctors for a user
     */
    fun getFavoriteDoctors(
        userId: String,
        onSuccess: (List<DoctorDetailData>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val favoriteDoctorIds = document.get("favoriteDoctors") as? List<String> ?: emptyList()
                
                if (favoriteDoctorIds.isEmpty()) {
                    onSuccess(emptyList())
                    return@addOnSuccessListener
                }
                
                // Fetch all favorite doctors
                db.collection("doctors")
                    .whereIn("__name__", favoriteDoctorIds)
                    .get()
                    .addOnSuccessListener { result ->
                        val doctors = result.mapNotNull { doc ->
                            try {
                                doc.toObject(DoctorDetailData::class.java).copy(id = doc.id)
                            } catch (e: Exception) {
                                null
                            }
                        }
                        onSuccess(doctors)
                    }
                    .addOnFailureListener { e ->
                        onFailure(e.message ?: "Gagal mengambil dokter favorit")
                    }
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Gagal mengambil data user")
            }
    }
}