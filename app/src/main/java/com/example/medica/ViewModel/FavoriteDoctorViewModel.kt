package com.example.medica.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medica.Model.DoctorDetailData
import com.example.medica.Repository.DoctorRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoriteDoctorUiState(
    val favoriteDoctors: List<DoctorDetailData> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class FavoriteDoctorViewModel : ViewModel() {

    private val doctorRepository = DoctorRepository()
    private val auth = FirebaseAuth.getInstance()
    
    private val _uiState = MutableStateFlow(FavoriteDoctorUiState())
    val uiState: StateFlow<FavoriteDoctorUiState> = _uiState.asStateFlow()

    init {
        loadFavoriteDoctors()
    }

    /**
     * Load favorite doctors for current user
     */
    fun loadFavoriteDoctors() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.update { it.copy(errorMessage = "User not logged in") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            doctorRepository.getFavoriteDoctors(
                userId = currentUser.uid,
                onSuccess = { doctors ->
                    _uiState.update {
                        it.copy(
                            favoriteDoctors = doctors,
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
     * Toggle favorite status for a doctor
     */
    fun toggleFavorite(
        doctorId: String,
        isFavorite: Boolean,
        onSuccess: () -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.update { it.copy(errorMessage = "User not logged in") }
            return
        }

        viewModelScope.launch {
            doctorRepository.toggleFavoriteDoctor(
                userId = currentUser.uid,
                doctorId = doctorId,
                isFavorite = !isFavorite, // Toggle
                onSuccess = {
                    loadFavoriteDoctors() // Refresh list
                    onSuccess()
                },
                onFailure = { error ->
                    _uiState.update { it.copy(errorMessage = error) }
                }
            )
        }
    }

    /**
     * Refresh favorite doctors list
     */
    fun refresh() {
        loadFavoriteDoctors()
    }
}
