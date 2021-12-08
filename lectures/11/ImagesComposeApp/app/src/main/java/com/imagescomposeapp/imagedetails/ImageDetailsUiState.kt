package com.imagescomposeapp.imagedetails

import com.imagescomposeapp.api.ImageData

sealed class ImageDetailsUiState {

    object Loading: ImageDetailsUiState()

    data class Success(
        val imageData: ImageData
    ): ImageDetailsUiState()

    data class LikeState(val isLiked: Boolean): ImageDetailsUiState()

    data class Error(
        val errorMessage: String
    ): ImageDetailsUiState()

}
