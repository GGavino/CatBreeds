package com.example.catbreeds.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.catbreeds.model.CatBreed
import com.example.catbreeds.viewmodel.FavoriteBreedsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteBreedsScreen(
    viewModel: FavoriteBreedsViewModel = hiltViewModel(),
    onBreedClick: (CatBreed) -> Unit = {}
) {
    val favoriteBreeds by viewModel.favoriteBreeds.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            favoriteBreeds.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No favorite breeds yet",
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Start adding breeds to your favorites by tapping the star icon",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(favoriteBreeds) { breed ->
                        BreedItem(
                            breed = breed,
                            onClick = { onBreedClick(breed) },
                            onFavoriteClick = { viewModel.toggleFavoriteStatus(it.id) }
                        )
                    }
                }
            }
        }
    }
}
