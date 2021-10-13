package com.example.composemovieapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navArgument
import androidx.navigation.compose.rememberNavController
import com.example.composemovieapp.movies.MoviesScreen
import com.example.composemovieapp.navigation.Screen.*
import androidx.navigation.compose.NavHost
import com.example.composemovieapp.details.MovieDetailsScreen


@Composable
fun MovieNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Movies.route) {
        composable(
            route = Movies.route
        ) {
            MoviesScreen(
                onMovieClick = { selectedMovie ->
                    navController.navigate("${MovieDetails.route}/$selectedMovie")
                }
            )
        }
        composable(
            route = "${MovieDetails.route}/{selectedMovie}",
            arguments = listOf(navArgument("selectedMovie") { type = NavType.StringType })
        ) { navBackStackEntry ->
            navBackStackEntry.arguments?.getString("selectedMovie")?.let { movie ->
                MovieDetailsScreen(selectedMovie = movie)
            }
        }

    }
}