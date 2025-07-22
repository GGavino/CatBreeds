package com.example.catbreeds.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.catbreeds.ui.screens.BreedDetailsScreen
import com.example.catbreeds.ui.screens.MainScreenWithDrawer
import com.example.catbreeds.viewmodel.MainViewModel

@Composable
fun CatBreedsNavigation(
    navController: NavHostController = rememberNavController()
) {
    val mainViewModel: MainViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = "main"
    ) {
        composable("main") {
            // Refresh data when returning to main screen
            LaunchedEffect(Unit) {
                mainViewModel.refreshBreedsData()
            }

            MainScreenWithDrawer(
                mainViewModel = mainViewModel,
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
