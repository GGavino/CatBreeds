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
class FavoriteBreedsViewModel @Inject constructor(
    private val repository: CatRepository
) : ViewModel() {

    private val _favoriteBreeds = MutableStateFlow<List<CatBreed>>(emptyList())
    val favoriteBreeds: StateFlow<List<CatBreed>> = _favoriteBreeds.asStateFlow()

    init {
        loadFavoriteBreeds()
    }

    private fun loadFavoriteBreeds() {
        viewModelScope.launch {
            repository.getFavoriteBreeds().collect { breeds ->
                _favoriteBreeds.value = breeds
            }
        }
    }

    fun toggleFavoriteStatus(breedId: String) {
        viewModelScope.launch {
            repository.toggleFavoriteStatus(breedId)
        }
    }
}
