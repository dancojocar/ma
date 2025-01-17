package com.example.composemovieapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.composemovieapp.movies.MoviesScreen
import com.example.composemovieapp.navigation.Screen.*
import androidx.navigation.compose.NavHost
import androidx.navigation.navArgument
import com.example.composemovieapp.details.MovieDetailsScreen
import com.example.composemovieapp.movies.AddMovieScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.navigation
import com.example.composemovieapp.movies.viewmodel.MoviesViewModel


@Composable
fun MovieNavigation() {
    val navController = rememberNavController()
    val viewModel: MoviesViewModel = hiltViewModel()

    NavHost(navController = navController, startDestination = Movies.route) {
        composable(route = Movies.route) {
            MoviesScreen(
                viewModel = viewModel,
                onMovieClick = { movieName ->
                    navController.navigate("${MovieDetails.route}/$movieName")
                },
                onAddMovieClick = {
                    navController.navigate(AddMovie.route)
                }
            )
        }

        composable(
            route = "${MovieDetails.route}/{movieName}",
            arguments = listOf(navArgument("movieName") { type = NavType.StringType })
        ) { backStackEntry ->
            MovieDetailsScreen(selectedMovie = backStackEntry.arguments?.getString("movieName") ?: "")
        }

        composable(route = AddMovie.route) {
            AddMovieScreen(
                viewModel= viewModel,
                onNavigateBack = {
                    navController.navigateUp()
                }
            )
        }
    }
}