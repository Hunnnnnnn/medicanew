package com.example.medica.ViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.medica.Model.PoliData
import com.example.medica.Repository.PoliRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PoliUiState(
    val poliList: List<PoliData> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class PoliViewModel : ViewModel() {

    private val poliRepository = PoliRepository()
    
    private val _uiState = MutableStateFlow(PoliUiState())
    val uiState: StateFlow<PoliUiState> = _uiState.asStateFlow()

    init {
        loadPolis()
    }

    /**
     * Load all poli/specialties
     */
    private fun loadPolis() {
        _uiState.update { it.copy(isLoading = true) }

        viewModelScope.launch {
            poliRepository.getAllPolis(
                onSuccess = { polis ->
                    _uiState.update {
                        it.copy(
                            poliList = polis,
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
}