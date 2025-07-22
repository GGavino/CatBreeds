package com.example.catbreeds.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.catbreeds.viewmodel.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenWithDrawer(
    onBreedClick: (com.example.catbreeds.model.CatBreed) -> Unit,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedScreen by remember { mutableStateOf(DrawerScreen.AllBreeds) }

    // Refresh breeds data when returning to AllBreeds to sync favorite status
    LaunchedEffect(selectedScreen) {
        if (selectedScreen == DrawerScreen.AllBreeds) {
            mainViewModel.refreshBreedsData()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Cat Breeds",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Home, contentDescription = "All Breeds") },
                        label = { Text("All Breeds") },
                        selected = selectedScreen == DrawerScreen.AllBreeds,
                        onClick = {
                            selectedScreen = DrawerScreen.AllBreeds
                            scope.launch { drawerState.close() }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                        label = { Text("Favorites") },
                        selected = selectedScreen == DrawerScreen.Favorites,
                        onClick = {
                            selectedScreen = DrawerScreen.Favorites
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            when (selectedScreen) {
                                DrawerScreen.AllBreeds -> "Cat Breeds"
                                DrawerScreen.Favorites -> "Favorite Breeds"
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                scope.launch { drawerState.open() }
                            }
                        ) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (selectedScreen) {
                    DrawerScreen.AllBreeds -> {
                        CatBreedsScreen(
                            viewModel = mainViewModel,
                            onBreedClick = onBreedClick
                        )
                    }
                    DrawerScreen.Favorites -> {
                        FavoriteBreedsScreen(onBreedClick = onBreedClick)
                    }
                }
            }
        }
    }
}

enum class DrawerScreen {
    AllBreeds,
    Favorites
}
