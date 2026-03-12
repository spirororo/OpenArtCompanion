package com.example.openartcompanion.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.openartcompanion.ui.screens.*

@Composable
fun NavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "feed"
    ) {

        composable("feed") {
            FeedScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }

        composable(
            "detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            DetailScreen(
                navController = navController,
                viewModel = hiltViewModel(),
                objectId = id
            )
        }

        composable("favourites") {
            FavoritesScreen(
                navController = navController,
                viewModel = hiltViewModel()
            )
        }
    }
}