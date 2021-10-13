package com.example.composemovieapp.navigation

sealed class Screen(val route: String) {
    object Movies : Screen(route = "movies")
    object MovieDetails : Screen(route = "details")
}