package com.imagescomposeapp.imageslist

import com.imagescomposeapp.api.ImageData

sealed class ImagesUiState {

    object Loading: ImagesUiState()

    data class Success(
        val images: List<ImageData>
    ): ImagesUiState()

    data class Error(
        val errorMessage: String
    ): ImagesUiState()

}
