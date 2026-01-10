package com.example.archdemo.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.example.archdemo.data.ItemsRepository
import com.example.archdemo.domain.GetItemsUseCase

class ArchDemoViewModel : ViewModel() {
    // Ideally injected with Hilt
    private val repository = ItemsRepository()
    private val getItemsUseCase = GetItemsUseCase(repository)

    private val _state = MutableStateFlow("Press to load")
    val state: StateFlow<String> = _state.asStateFlow()

    fun loadData() {
        _state.value = getItemsUseCase.execute()
    }
}

@Composable
fun ArchDemoApp(viewModel: ArchDemoViewModel) {
    val state by viewModel.state.collectAsState()

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = state, modifier = Modifier.padding(16.dp))
            Button(onClick = { viewModel.loadData() }) {
                Text("Load Data")
            }
        }
    }
}
