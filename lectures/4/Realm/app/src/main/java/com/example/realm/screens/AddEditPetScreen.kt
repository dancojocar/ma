package com.example.realm.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query

@Composable
fun AddEditPetScreen(navController: NavController, petViewModel: PetViewModel) {
    val realm = RealmManager.realm
    var name by remember { mutableStateOf("") }
    var petType by remember { mutableStateOf("Cat") }

    val pet = petViewModel.pet

    LaunchedEffect(pet) {
        pet?.let { pet: Pet ->
            name = pet.name
            petType = if (pet is Cat) "Cat" else "Dog"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Pet Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            RadioButton(
                selected = petType == "Cat",
                onClick = { petType = "Cat" },
                enabled = pet == null
            )
            Text(text = "Cat", modifier = Modifier.align(Alignment.CenterVertically))
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(
                selected = petType == "Dog",
                onClick = { petType = "Dog" },
                enabled = pet == null
            )
            Text(text = "Dog", modifier = Modifier.align(Alignment.CenterVertically))
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                realm.writeBlocking {
                    if (pet == null) {
                        if (petType == "Cat") {
                            copyToRealm(Cat().apply { this.name = name })
                        } else {
                            copyToRealm(Dog().apply { this.name = name })
                        }
                    } else {
                        if (pet is Cat) {
                            findLatest(pet)?.name = name
                        } else if (pet is Dog) {
                            findLatest(pet)?.name = name
                        }
                    }
                }
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
    }
}
