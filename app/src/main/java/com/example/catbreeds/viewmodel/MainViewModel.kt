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
class MainViewModel @Inject constructor(
    private val repository: CatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    companion object {
        private const val PAGE_SIZE = 10
        private const val TOTAL_BREEDS = 67 // Cat API has approximately 67 breeds
    }

    init {
        loadBreeds()
    }

    private fun loadBreeds(page: Int = 0) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.getBreeds(limit = PAGE_SIZE, page = page).fold(
                onSuccess = { breeds ->
                    val totalPages = (TOTAL_BREEDS + PAGE_SIZE - 1) / PAGE_SIZE // Ceiling division
                    _uiState.value = _uiState.value.copy(
                        breeds = breeds,
                        isLoading = false,
                        error = null,
                        currentPage = page,
                        totalPages = totalPages,
                        hasNextPage = page < totalPages - 1,
                        hasPreviousPage = page > 0,
                        isSearching = false
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        breeds = emptyList(),
                        isLoading = false,
                        error = exception.message ?: "Unknown error occurred"
                    )
                }
            )
        }
    }

    fun searchBreeds(query: String) {
        if (query.isBlank()) {
            loadBreeds()
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            repository.searchBreeds(query).fold(
                onSuccess = { breeds ->
                    _uiState.value = _uiState.value.copy(
                        breeds = breeds,
                        isLoading = false,
                        error = null,
                        searchQuery = query,
                        isSearching = true,
                        // Reset pagination for search results
                        currentPage = 0,
                        totalPages = 1,
                        hasNextPage = false,
                        hasPreviousPage = false
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        breeds = emptyList(),
                        isLoading = false,
                        error = exception.message ?: "Unknown error occurred"
                    )
                }
            )
        }
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(searchQuery = "", isSearching = false)
        loadBreeds()
    }

    fun goToFirstPage() {
        if (_uiState.value.currentPage != 0 && !_uiState.value.isLoading) {
            loadBreeds(0)
        }
    }

    fun goToPreviousPage() {
        val currentPage = _uiState.value.currentPage
        if (currentPage > 0 && !_uiState.value.isLoading) {
            loadBreeds(currentPage - 1)
        }
    }

    fun goToNextPage() {
        val currentPage = _uiState.value.currentPage
        if (_uiState.value.hasNextPage && !_uiState.value.isLoading) {
            loadBreeds(currentPage + 1)
        }
    }

    fun goToLastPage() {
        val lastPage = _uiState.value.totalPages - 1
        if (_uiState.value.currentPage != lastPage && !_uiState.value.isLoading) {
            loadBreeds(lastPage)
        }
    }

    fun goToPage(page: Int) {
        val targetPage = page.coerceIn(0, _uiState.value.totalPages - 1)
        if (targetPage != _uiState.value.currentPage && !_uiState.value.isLoading) {
            loadBreeds(targetPage)
        }
    }

    fun retry() {
        val currentState = _uiState.value
        if (currentState.isSearching) {
            searchBreeds(currentState.searchQuery)
        } else {
            loadBreeds(currentState.currentPage)
        }
    }
}

data class MainUiState(
    val breeds: List<CatBreed> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 0,
    val totalPages: Int = 1,
    val hasNextPage: Boolean = false,
    val hasPreviousPage: Boolean = false,
    val searchQuery: String = "",
    val isSearching: Boolean = false
)
