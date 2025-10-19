package com.example.realm.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.realm.model.Pet

class PetViewModel : ViewModel() {
    var pet by mutableStateOf<Pet?>(null)
}
