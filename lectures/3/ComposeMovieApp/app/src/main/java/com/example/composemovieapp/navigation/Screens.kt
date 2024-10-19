package com.example.composemovieapp.navigation

sealed class Screen(val route: String) {
    data object Movies : Screen(route = "movies")
    data object MovieDetails : Screen(route = "details")
}