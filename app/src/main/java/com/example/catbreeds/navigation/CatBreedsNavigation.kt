package com.example.catbreeds.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.catbreeds.ui.screens.BreedDetailsScreen
import com.example.catbreeds.ui.screens.CatBreedsScreen
import com.example.catbreeds.ui.screens.FavoriteBreedsScreen
import com.example.catbreeds.viewmodel.MainViewModel
import com.example.catbreeds.viewmodel.FavoriteBreedsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatBreedsNavigation(
    navController: NavHostController = rememberNavController()
) {
    val mainViewModel: MainViewModel = hiltViewModel()
    val favoriteBreedsViewModel: FavoriteBreedsViewModel = hiltViewModel()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Track current route for drawer selection
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

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
                        selected = currentRoute == "breeds_list",
                        onClick = {
                            navController.navigate("breeds_list") {
                                popUpTo("breeds_list") { inclusive = true }
                            }
                            scope.launch { drawerState.close() }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                        label = { Text("Favorites") },
                        selected = currentRoute == "favorites_list",
                        onClick = {
                            navController.navigate("favorites_list")
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
                            when (currentRoute) {
                                "breeds_list" -> "Cat Breeds"
                                "favorites_list" -> "Favorite Breeds"
                                else -> "Cat Breeds"
                            }
                        )
                    },
                    navigationIcon = {
                        if (currentRoute != "breed_details/{breedId}") {
                            IconButton(
                                onClick = {
                                    scope.launch { drawerState.open() }
                                }
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = "breeds_list",
                modifier = Modifier.padding(paddingValues)
            ) {
                composable("breeds_list") {
                    LaunchedEffect(Unit) {
                        mainViewModel.refreshBreedsData()
                    }

                    CatBreedsScreen(
                        viewModel = mainViewModel,
                        onBreedClick = { breed ->
                            navController.navigate("breed_details/${breed.id}")
                        }
                    )
                }

                composable("favorites_list") {
                    FavoriteBreedsScreen(
                        viewModel = favoriteBreedsViewModel,
                        onBreedClick = { breed ->
                            navController.navigate("breed_details/${breed.id}")
                        }
                    )
                }

                composable("breed_details/{breedId}") { backStackEntry ->
                    val breedId = backStackEntry.arguments?.getString("breedId") ?: ""
                    BreedDetailsScreen(
                        breedId = breedId,
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
