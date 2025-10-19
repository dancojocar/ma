package com.example.realm.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.realm.data.RealmManager
import com.example.realm.model.Cat
import com.example.realm.model.Dog
import com.example.realm.model.Pet
import com.example.realm.viewModel.PetViewModel
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.combine

@Composable
fun PetListScreen(navController: NavController, petViewModel: PetViewModel) {
    val realm = RealmManager.realm
    var pets by remember { mutableStateOf(emptyList<Pet>()) }

    LaunchedEffect(Unit) {
        val catsFlow = realm.query<Cat>().find().asFlow()
        val dogsFlow = realm.query<Dog>().find().asFlow()

        combine(catsFlow, dogsFlow) { cats, dogs ->
            (cats.list + dogs.list).sortedBy { it.name }
        }.collect { pets = it }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                petViewModel.pet = null
                navController.navigate("addEditPet") 
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Pet")
            }
        }
    ) {
        LazyColumn(modifier = Modifier.padding(it)) {
            items(pets) { pet ->
                PetItem(
                    pet = pet,
                    onEdit = { 
                        petViewModel.pet = pet
                        navController.navigate("addEditPet") 
                    },
                    onDelete = {
                        realm.writeBlocking {
                            val petToDelete = findLatest(pet)
                            petToDelete?.let { delete(it) }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PetItem(pet: Pet, onEdit: () -> Unit, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = pet.name, modifier = Modifier.weight(1f))
        IconButton(onClick = onEdit) {
            Icon(Icons.Filled.Edit, contentDescription = "Edit Pet")
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Delete Pet")
        }
    }
}
