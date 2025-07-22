package com.example.catbreeds.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.catbreeds.viewmodel.BreedDetailsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BreedDetailsScreen(
    breedId: String,
    onBackClick: () -> Unit,
    viewModel: BreedDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(breedId) {
        viewModel.loadBreedDetails(breedId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.breed?.name ?: "Breed Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Only show favorite button when breed data is loaded
                    uiState.breed?.let { breed ->
                        IconButton(
                            onClick = { viewModel.toggleFavoriteStatus() }
                        ) {
                            Icon(
                                imageVector = if (breed.isFavorite) {
                                    Icons.Filled.Star
                                } else {
                                    Icons.Outlined.StarBorder
                                },
                                contentDescription = if (breed.isFavorite) {
                                    "Remove from favorites"
                                } else {
                                    "Add to favorites"
                                },
                                tint = if (breed.isFavorite) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error: ${uiState.error}",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                        Button(onClick = { viewModel.loadBreedDetails(breedId) }) {
                            Text("Retry")
                        }
                    }
                }
            }

            uiState.breed != null -> {
                BreedDetailsContent(
                    breed = uiState.breed!!,
                    paddingValues = paddingValues
                )
            }
        }
    }
}

@Composable
private fun BreedDetailsContent(
    breed: com.example.catbreeds.model.CatBreed,
    paddingValues: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(breed.image?.url)
                    .crossfade(true)
                    .build(),
                contentDescription = "Image of ${breed.name}",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = breed.name,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            // Display origin and life span on the same row for space efficiency
            if (!breed.origin.isNullOrBlank() || !breed.life_span.isNullOrBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (!breed.origin.isNullOrBlank()) {
                        Row(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Origin: ",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = breed.origin,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }

                    if (!breed.life_span.isNullOrBlank()) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = if (breed.origin.isNullOrBlank())
                                Arrangement.Start else Arrangement.End
                        ) {
                            Text(
                                text = "Life Span: ",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${breed.life_span} years",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }

                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
            }

            if (!breed.description.isNullOrBlank()) {
                BreedInfoItem(
                    label = "Description",
                    content = breed.description
                )
            }

            if (!breed.temperament.isNullOrBlank()) {
                BreedInfoItem(
                    label = "Temperament",
                    content = breed.temperament
                )
            }
        }
    }
}

@Composable
private fun BreedInfoItem(
    label: String,
    content: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row {
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
            )
        }
    }
}
