package com.example.catbreeds.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catbreeds.model.CatBreed
import com.example.catbreeds.repository.CatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BreedDetailsViewModel @Inject constructor(
    private val repository: CatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BreedDetailsUiState())
    val uiState: StateFlow<BreedDetailsUiState> = _uiState.asStateFlow()

    fun loadBreedDetails(breedId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = repository.getBreedById(breedId)

            if (result.isSuccess) {
                val breed = result.getOrNull()
                _uiState.value = _uiState.value.copy(
                    breed = breed,
                    isLoading = false,
                    error = null
                )
            } else {
                val exception = result.exceptionOrNull()
                _uiState.value = _uiState.value.copy(
                    breed = null,
                    isLoading = false,
                    error = exception?.message ?: "Failed to load breed details"
                )
            }
        }
    }

    fun toggleFavoriteStatus() {
        val currentBreed = _uiState.value.breed ?: return

        viewModelScope.launch {
            val result = repository.toggleFavoriteStatus(currentBreed.id)

            if (result.isSuccess) {
                // Immediate UI feedback - update local state before database confirms
                val updatedBreed = currentBreed.copy(isFavorite = !currentBreed.isFavorite)
                _uiState.value = _uiState.value.copy(breed = updatedBreed)
            } else {
                val error = result.exceptionOrNull()?.message ?: "Failed to update favorite status"
                _uiState.value = _uiState.value.copy(error = error)
            }
        }
    }
}

data class BreedDetailsUiState(
    val breed: CatBreed? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
