package com.imagescomposeapp.imagedetails

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imagescomposeapp.api.ImagesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ImageDetailsViewModel @Inject constructor(
    private val repository: ImagesRepository
): ViewModel() {

    private val _uiState = MutableStateFlow<ImageDetailsUiState>(ImageDetailsUiState.Loading)
    val uiState = _uiState.asStateFlow()

    var isLiked = mutableStateOf(false)

    fun getImage(imageId: Int?) {
        viewModelScope.launch {
            _uiState.value = ImageDetailsUiState.Loading
            val image = repository.getImage(imageId)
            if (image != null) {
                _uiState.value = ImageDetailsUiState.Success(image)
            } else {
                _uiState.value = ImageDetailsUiState.Error("Failed to load image.")
            }
        }
    }

    fun likeImage(imageId: Int) {
        isLiked.value = true

        viewModelScope.launch {
            val result = repository.likeImage(imageId)
            result.onFailure {
                isLiked.value = false
            }
        }
    }

}
