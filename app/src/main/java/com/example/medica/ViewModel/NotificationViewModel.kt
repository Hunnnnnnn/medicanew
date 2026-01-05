package com.example.medica.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medica.Model.NotificationData
import com.example.medica.Repository.NotificationRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationUiState(
    val notifications: List<NotificationData> = emptyList(),
    val unreadCount: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class NotificationViewModel : ViewModel() {

    private val notificationRepository = NotificationRepository()
    private val auth = FirebaseAuth.getInstance()
    
    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    /**
     * Load all notifications for current user
     */
    fun loadNotifications() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.update { it.copy(errorMessage = "User not logged in") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            notificationRepository.getUserNotifications(
                userId = currentUser.uid,
                onSuccess = { notifications ->
                    val unread = notifications.count { !it.isRead }
                    _uiState.update {
                        it.copy(
                            notifications = notifications,
                            unreadCount = unread,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error
                        )
                    }
                }
            )
        }
    }

    /**
     * Mark notification as read
     */
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(
                notificationId = notificationId,
                onSuccess = {
                    loadNotifications() // Refresh list
                },
                onFailure = { error ->
                    _uiState.update { it.copy(errorMessage = error) }
                }
            )
        }
    }

    /**
     * Delete notification
     */
    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(
                notificationId = notificationId,
                onSuccess = {
                    loadNotifications() // Refresh list
                },
                onFailure = { error ->
                    _uiState.update { it.copy(errorMessage = error) }
                }
            )
        }
    }

    /**
     * Refresh notifications list
     */
    fun refresh() {
        loadNotifications()
    }
}