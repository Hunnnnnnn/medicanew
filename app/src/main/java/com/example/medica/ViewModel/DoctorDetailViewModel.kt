package com.example.medica.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medica.Model.DoctorDetailData
import com.example.medica.Repository.DoctorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DoctorDetailUiState(
    val doctor: DoctorDetailData? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isFavorite: Boolean = false
)

class DoctorDetailViewModel : ViewModel() {

    private val doctorRepository = DoctorRepository()
    
    private val _uiState = MutableStateFlow(DoctorDetailUiState())
    val uiState: StateFlow<DoctorDetailUiState> = _uiState.asStateFlow()

    /**
     * Load doctor details by ID
     */
    fun loadDoctor(doctorId: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            doctorRepository.getDoctorById(
                doctorId = doctorId,
                onSuccess = { doctor ->
                    _uiState.update {
                        it.copy(
                            doctor = doctor,
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
     * Toggle favorite status
     */
    fun toggleFavorite(
        userId: String,
        doctorId: String,
        onSuccess: () -> Unit
    ) {
        val currentFavoriteStatus = _uiState.value.isFavorite

        viewModelScope.launch {
            doctorRepository.toggleFavoriteDoctor(
                userId = userId,
                doctorId = doctorId,
                isFavorite = !currentFavoriteStatus,
                onSuccess = {
                    _uiState.update { it.copy(isFavorite = !currentFavoriteStatus) }
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(errorMessage = error) }
                }
            )
        }
    }

    /**
     * Set favorite status (for initial load)
     */
    fun setFavoriteStatus(isFavorite: Boolean) {
        _uiState.update { it.copy(isFavorite = isFavorite) }
    }
}
