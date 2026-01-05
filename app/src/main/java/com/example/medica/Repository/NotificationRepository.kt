package com.example.medica.Repository

import android.util.Log
import com.example.medica.Model.NotificationData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class NotificationRepository {
    private val db = FirebaseFirestore.getInstance()
    private val notificationsCollection = db.collection("notifications")

    /**
     * Get all notifications for a user with REAL-TIME UPDATES
     * ⚠️ Uses addSnapshotListener for live sync!
     */
    fun getUserNotifications(
        userId: String,
        onSuccess: (List<NotificationData>) -> Unit,
        onFailure: (String) -> Unit
    ) {
        Log.d("NotificationRepo", "Setting up REAL-TIME listener for userId: $userId")
        
        notificationsCollection
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("NotificationRepo", "Listener error: ${error.message}", error)
                    onFailure(error.message ?: "Gagal mengambil notifikasi")
                    return@addSnapshotListener
                }
                
                if (snapshot == null) {
                    Log.w("NotificationRepo", "Snapshot is null")
                    onSuccess(emptyList())
                    return@addSnapshotListener
                }
                
                val notifications = snapshot.documents.mapNotNull { document ->
                    try {
                        document.toObject(NotificationData::class.java)?.copy(id = document.id)
                    } catch (e: Exception) {
                        Log.e("NotificationRepo", "Parse error: ${e.message}", e)
                        null
                    }
                }
                
                Log.d("NotificationRepo", "✅ Loaded ${notifications.size} notifications (LIVE)")
                onSuccess(notifications)
            }
    }

    /**
     * Mark notification as read
     */
    fun markAsRead(
        notificationId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        notificationsCollection.document(notificationId)
            .update("isRead", true)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Gagal update notifikasi")
            }
    }

    /**
     * Create notification (system generated)
     */
    fun createNotification(
        notification: NotificationData,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        notificationsCollection
            .add(notification)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Gagal membuat notifikasi")
            }
    }

    /**
     * Delete notification
     */
    fun deleteNotification(
        notificationId: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        notificationsCollection.document(notificationId)
            .delete()
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Gagal menghapus notifikasi")
            }
    }

    /**
     * Get unread notification count
     */
    fun getUnreadCount(
        userId: String,
        onSuccess: (Int) -> Unit,
        onFailure: (String) -> Unit
    ) {
        notificationsCollection
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { documents ->
                onSuccess(documents.size())
            }
            .addOnFailureListener { e ->
                onFailure(e.message ?: "Gagal menghitung notifikasi")
            }
    }
}
