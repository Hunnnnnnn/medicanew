package com.example.medica.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medica.Model.DoctorDetailData
import com.example.medica.Repository.DoctorRepository
import com.example.medica.Repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val doctorList: List<DoctorDetailData> = emptyList(),
    val userName: String = "User",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedSpecialty: String? = null
)

class HomeViewModel : ViewModel() {
    
    private val doctorRepository = DoctorRepository()
    private val userRepository = UserRepository()
    private val auth = FirebaseAuth.getInstance()
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUserData()
        loadDoctors()
    }
    
    /**
     * Load current user data from Firebase
     */
    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            viewModelScope.launch {
                userRepository.getUserData(
                    userId = currentUser.uid,
                    onSuccess = { userData ->
                        _uiState.update {
                            it.copy(userName = userData.name)
                        }
                    },
                    onFailure = { error ->
                        // Keep default "User" if fail to load
                    }
                )
            }
        }
    }

    /**
     * Load all doctors from Firebase
     */
    fun loadDoctors() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        
        viewModelScope.launch {
            doctorRepository.getDoctors(
                onSuccess = { doctors ->
                    _uiState.update {
                        it.copy(
                            doctorList = doctors,
                            isLoading = false,
                            errorMessage = null
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
     * Filter doctors by specialty
     */
    fun filterBySpecialty(specialty: String) {
        _uiState.update { it.copy(isLoading = true, selectedSpecialty = specialty) }
        
        viewModelScope.launch {
            doctorRepository.getDoctorsBySpecialty(
                specialty = specialty,
                onSuccess = { doctors ->
                    _uiState.update {
                        it.copy(
                            doctorList = doctors,
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
     * Clear filter and show all doctors
     */
    fun clearFilter() {
        _uiState.update { it.copy(selectedSpecialty = null) }
        loadDoctors()
    }

    /**
     * Refresh doctors list
     */
    fun refresh() {
        if (_uiState.value.selectedSpecialty != null) {
            filterBySpecialty(_uiState.value.selectedSpecialty!!)
        } else {
            loadDoctors()
        }
    }
}