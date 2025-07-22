package com.example.catbreeds.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.catbreeds.model.CatBreed
import com.example.catbreeds.viewmodel.MainViewModel
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatBreedsScreen(
    viewModel: MainViewModel = hiltViewModel(),
    onBreedClick: (CatBreed) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cat Breeds") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { query ->
                    searchQuery = query
                    viewModel.searchBreeds(query)
                },
                onClearSearch = {
                    searchQuery = ""
                    viewModel.clearSearch()
                },
                isSearching = uiState.isSearching,
                modifier = Modifier.padding(16.dp)
            )

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }

                uiState.error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
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
                            Button(onClick = { viewModel.retry() }) {
                                Text("Retry")
                            }
                        }
                    }
                }

                uiState.breeds.isEmpty() && uiState.isSearching -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Text(
                            text = "No breeds found for \"${uiState.searchQuery}\"",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.align(Alignment.Center),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.breeds) { breed ->
                            BreedItem(
                                breed = breed,
                                onClick = { onBreedClick(breed) },
                                onFavoriteClick = { viewModel.toggleFavoriteStatus(it.id) }
                            )
                        }
                    }
                }
            }

            // Hide pagination when searching as results are not paginated
            if (!uiState.isSearching) {
                PaginationControls(
                    currentPage = uiState.currentPage,
                    totalPages = uiState.totalPages,
                    hasNextPage = uiState.hasNextPage,
                    hasPreviousPage = uiState.hasPreviousPage,
                    isLoading = uiState.isLoading,
                    onFirstPage = viewModel::goToFirstPage,
                    onPreviousPage = viewModel::goToPreviousPage,
                    onNextPage = viewModel::goToNextPage,
                    onLastPage = viewModel::goToLastPage,
                    onGoToPage = viewModel::goToPage
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaginationControls(
    currentPage: Int,
    totalPages: Int,
    hasNextPage: Boolean,
    hasPreviousPage: Boolean,
    isLoading: Boolean,
    onFirstPage: () -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onLastPage: () -> Unit,
    onGoToPage: (Int) -> Unit
) {
    var showGoToPageDialog by remember { mutableStateOf(false) }
    var pageInput by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onFirstPage,
                    enabled = hasPreviousPage && !isLoading,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = "First Page",
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onPreviousPage,
                    enabled = hasPreviousPage && !isLoading,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Page",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${currentPage + 1} / $totalPages",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                IconButton(
                    onClick = { showGoToPageDialog = true },
                    enabled = !isLoading && totalPages > 1,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.MoreHoriz,
                        contentDescription = "Go to page",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNextPage,
                    enabled = hasNextPage && !isLoading,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Page",
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onLastPage,
                    enabled = hasNextPage && !isLoading,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = "Last Page",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    // Go to Page Dialog
    if (showGoToPageDialog) {
        AlertDialog(
            onDismissRequest = {
                showGoToPageDialog = false
                pageInput = ""
            },
            title = {
                Text(
                    text = "Go to Page",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter page number (1 - $totalPages):",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = pageInput,
                        onValueChange = {
                            // Restrict input to digits only and limit length
                            if (it.all { char -> char.isDigit() } && it.length <= 3) {
                                pageInput = it
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Go
                        ),
                        keyboardActions = KeyboardActions(
                            onGo = {
                                val targetPage = pageInput.toIntOrNull()?.minus(1)
                                if (targetPage != null && targetPage in 0 until totalPages) {
                                    onGoToPage(targetPage)
                                    showGoToPageDialog = false
                                    pageInput = ""
                                }
                            }
                        ),
                        singleLine = true,
                        placeholder = { Text("Page number") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val targetPage = pageInput.toIntOrNull()?.minus(1)
                        if (targetPage != null && targetPage in 0 until totalPages) {
                            onGoToPage(targetPage)
                            showGoToPageDialog = false
                            pageInput = ""
                        }
                    },
                    enabled = pageInput.isNotBlank() &&
                             pageInput.toIntOrNull()?.let { it in 1..totalPages } == true
                ) {
                    Text("Go")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showGoToPageDialog = false
                        pageInput = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClearSearch: () -> Unit,
    isSearching: Boolean,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text("Search cat breeds...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Search"
            )
        },
        trailingIcon = {
            if (query.isNotEmpty() || isSearching) {
                IconButton(onClick = onClearSearch) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Clear search"
                    )
                }
            }
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = { /* Search is triggered on text change */ }
        )
    )
}

@Composable
fun BreedItem(
    breed: CatBreed,
    onClick: () -> Unit,
    onFavoriteClick: (CatBreed) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Cat Image
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(breed.image?.url)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Image of ${breed.name}",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Breed Name
                Text(
                    text = breed.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Clickable favorite star with visual state based on favorite status
            IconButton(
                onClick = { onFavoriteClick(breed) },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(32.dp)
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
                        Color(0xFFFFD700) // Golden yellow for favorited
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    },
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
