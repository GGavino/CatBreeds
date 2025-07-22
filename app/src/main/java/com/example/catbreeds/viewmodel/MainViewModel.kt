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
    }

    private var totalBreedsFromApi: Int? = null

    init {
        initializeApp()
    }

    private fun initializeApp() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            repository.initializeAppData().fold(
                onSuccess = { totalCount ->
                    totalBreedsFromApi = totalCount
                    loadBreeds(0)
                },
                onFailure = {
                    checkCacheStatus()
                    loadBreeds(0)
                }
            )
        }
    }

    private fun checkCacheStatus() {
        viewModelScope.launch {
            val hasCachedData = repository.hasCachedData()
            _uiState.value = _uiState.value.copy(hasCachedData = hasCachedData)
        }
    }

    private fun loadBreeds(page: Int = 0) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val isOnline = repository.isOnline()
            val totalBreedsCount = if (isOnline && totalBreedsFromApi != null) {
                totalBreedsFromApi!!
            } else {
                repository.getTotalBreedsCount().getOrElse {
                    repository.getCachedBreedsCount()
                }
            }

            repository.getBreeds(limit = PAGE_SIZE, page = page).fold(
                onSuccess = { breeds ->
                    val totalPages = (totalBreedsCount + PAGE_SIZE - 1) / PAGE_SIZE

                    _uiState.value = _uiState.value.copy(
                        breeds = breeds,
                        isLoading = false,
                        error = null,
                        currentPage = page,
                        totalPages = totalPages,
                        hasNextPage = page < totalPages - 1,
                        hasPreviousPage = page > 0,
                        isSearching = false,
                        isOnline = isOnline,
                        isOfflineData = !isOnline && breeds.isNotEmpty(),
                        totalBreedsCount = totalBreedsCount
                    )
                    checkCacheStatus()
                },
                onFailure = { exception ->
                    val isNetworkError = isNetworkError(exception.message)
                    val totalPages = (totalBreedsCount + PAGE_SIZE - 1) / PAGE_SIZE

                    _uiState.value = _uiState.value.copy(
                        breeds = emptyList(),
                        isLoading = false,
                        error = exception.message ?: "Unknown error occurred",
                        totalPages = totalPages,
                        hasNextPage = page < totalPages - 1,
                        hasPreviousPage = page > 0,
                        isOnline = isOnline,
                        isOfflineData = isNetworkError && _uiState.value.hasCachedData,
                        totalBreedsCount = totalBreedsCount
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
            val isOnline = repository.isOnline()

            repository.searchBreeds(query).fold(
                onSuccess = { breeds ->
                    _uiState.value = _uiState.value.copy(
                        breeds = breeds,
                        isLoading = false,
                        error = null,
                        searchQuery = query,
                        isSearching = true,
                        currentPage = 0,
                        totalPages = 1,
                        hasNextPage = false,
                        hasPreviousPage = false,
                        isOnline = isOnline,
                        isOfflineData = !isOnline && breeds.isNotEmpty()
                    )
                    checkCacheStatus()
                },
                onFailure = { exception ->
                    val isNetworkError = isNetworkError(exception.message)
                    _uiState.value = _uiState.value.copy(
                        breeds = emptyList(),
                        isLoading = false,
                        error = exception.message ?: "Unknown error occurred",
                        searchQuery = query,
                        isSearching = true,
                        isOnline = isOnline,
                        isOfflineData = isNetworkError && _uiState.value.hasCachedData
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

    fun refreshData() {
        initializeApp()
    }

    fun clearCache() {
        viewModelScope.launch {
            repository.clearCache().fold(
                onSuccess = {
                    totalBreedsFromApi = null
                    checkCacheStatus()
                    initializeApp()
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = "Failed to clear cache: ${exception.message}"
                    )
                }
            )
        }
    }

    fun toggleFavoriteStatus(breedId: String) {
        viewModelScope.launch {
            val result = repository.toggleFavoriteStatus(breedId)

            if (result.isSuccess) {
                // Update local state immediately for better UX
                val currentBreeds = _uiState.value.breeds
                val updatedBreeds = currentBreeds.map { breed ->
                    if (breed.id == breedId) {
                        breed.copy(isFavorite = !breed.isFavorite)
                    } else {
                        breed
                    }
                }
                _uiState.value = _uiState.value.copy(breeds = updatedBreeds)
            } else {
                val error = result.exceptionOrNull()?.message ?: "Failed to update favorite status"
                _uiState.value = _uiState.value.copy(error = error)
            }
        }
    }

    // Checks for network-related error patterns
    private fun isNetworkError(errorMessage: String?): Boolean {
        return errorMessage?.contains("network", ignoreCase = true) == true ||
                errorMessage?.contains("connection", ignoreCase = true) == true ||
                errorMessage?.contains("timeout", ignoreCase = true) == true ||
                errorMessage?.contains("unreachable", ignoreCase = true) == true
    }

    fun refreshBreedsData() {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState.isSearching) {
                searchBreeds(currentState.searchQuery)
            } else {
                loadBreeds(currentState.currentPage)
            }
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
    val isSearching: Boolean = false,
    val hasCachedData: Boolean = false,
    val isOfflineData: Boolean = false,
    val isOnline: Boolean = true,
    val totalBreedsCount: Int = 0
)
