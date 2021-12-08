package com.imagescomposeapp.imageslist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imagescomposeapp.api.ImagesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImagesViewModel @Inject constructor(
    private val repository: ImagesRepository
): ViewModel() {

    private val _uiState = MutableStateFlow<ImagesUiState>(ImagesUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val images = repository.getImages()
            _uiState.value = ImagesUiState.Success(images = images)
        }
    }

    fun searchImages(query: String) {
        viewModelScope.launch {
            _uiState.value = ImagesUiState.Loading
            val images = repository.searchImages(query = query)
            _uiState.value = ImagesUiState.Success(images = images)
        }
    }

}
