package com.example.realm.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.realm.screens.AddEditPetScreen
import com.example.realm.screens.PetListScreen
import com.example.realm.viewModel.PetViewModel

@Composable
fun AppNavigator() {
    val navController = rememberNavController()
    val petViewModel: PetViewModel = viewModel()
    NavHost(navController = navController, startDestination = "petList") {
        composable("petList") {
            PetListScreen(navController = navController, petViewModel = petViewModel)
        }
        composable("addEditPet") {
            AddEditPetScreen(navController = navController, petViewModel = petViewModel)
        }
    }
}
