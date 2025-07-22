package com.example.catbreeds.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.catbreeds.ui.screens.BreedDetailsScreen
import com.example.catbreeds.ui.screens.CatBreedsScreen

@Composable
fun CatBreedsNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "breeds_list"
    ) {
        composable("breeds_list") {
            CatBreedsScreen(
                viewModel = hiltViewModel(),
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
