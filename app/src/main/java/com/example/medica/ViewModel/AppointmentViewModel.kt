package com.example.medica.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medica.Model.AppointmentData
import com.example.medica.Repository.AppointmentRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppointmentUiState(
    val appointments: List<AppointmentData> = emptyList(),
    val selectedTab: String = "upcoming",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AppointmentViewModel : ViewModel() {

    private val appointmentRepository = AppointmentRepository()
    private val auth = FirebaseAuth.getInstance()
    
    private val _uiState = MutableStateFlow(AppointmentUiState())
    val uiState: StateFlow<AppointmentUiState> = _uiState.asStateFlow()

    // Filtered appointments based on selected tab
    val filteredAppointments: StateFlow<List<AppointmentData>> = 
        MutableStateFlow(emptyList<AppointmentData>()).apply {
            viewModelScope.launch {
                _uiState.collect { state ->
                    value = state.appointments.filter { it.status == state.selectedTab }
                }
            }
        }

    init {
        loadAppointments()
    }

    /**
     * Load all appointments for current user
     */
    fun loadAppointments() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            android.util.Log.e("AppointmentVM", "User not logged in")
            _uiState.update { it.copy(errorMessage = "User not logged in") }
            return
        }

        android.util.Log.d("AppointmentVM", "Loading appointments for user: ${currentUser.uid}")
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            appointmentRepository.getUserAppointments(
                userId = currentUser.uid,
                onSuccess = { appointments ->
                    android.util.Log.d("AppointmentVM", "Loaded ${appointments.size} appointments")
                    _uiState.update {
                        it.copy(
                            appointments = appointments,
                            isLoading = false
                        )
                    }
                },
                onFailure = { error ->
                    android.util.Log.e("AppointmentVM", "Error loading appointments: $error")
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
     * Change selected tab
     */
    fun onTabSelected(tab: String) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    /**
     * Create new appointment
     */
    fun createAppointment(
        appointment: AppointmentData,
        onSuccess: () -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.update { it.copy(errorMessage = "User not logged in") }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        val appointmentWithUser = appointment.copy(userId = currentUser.uid)

        viewModelScope.launch {
            appointmentRepository.createAppointment(
                appointment = appointmentWithUser,
                onSuccess = {
                    loadAppointments() // Refresh list
                    onSuccess()
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
     * Reschedule appointment
     */
    fun rescheduleAppointment(
        appointmentId: String,
        newDate: String,
        newTime: String,
        onSuccess: () -> Unit
    ) {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            appointmentRepository.rescheduleAppointment(
                appointmentId = appointmentId,
                newDate = newDate,
                newTime = newTime,
                onSuccess = {
                    loadAppointments() // Refresh list
                    onSuccess()
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
     * Cancel appointment
     */
    fun cancelAppointment(
        appointmentId: String,
        onSuccess: () -> Unit
    ) {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            appointmentRepository.cancelAppointment(
                appointmentId = appointmentId,
                onSuccess = {
                    loadAppointments() // Refresh list
                    onSuccess()
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
     * Delete appointment
     */
    fun deleteAppointment(
        appointmentId: String,
        onSuccess: () -> Unit
    ) {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            appointmentRepository.deleteAppointment(
                appointmentId = appointmentId,
                onSuccess = {
                    loadAppointments() // Refresh list
                    onSuccess()
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
     * Refresh appointments list
     */
    fun refresh() {
        loadAppointments()
    }
}