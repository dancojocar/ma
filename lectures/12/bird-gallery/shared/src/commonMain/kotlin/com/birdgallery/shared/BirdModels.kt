package com.birdgallery.shared

data class BirdImage(
    val author: String,
    val category: String,
    val path: String
)

data class BirdsUiState(
    val images: List<BirdImage> = emptyList(),
    val selectedCategory: String? = null
) {
    val categories: Set<String> = images.map { it.category }.toSet()
    val selectedImages: List<BirdImage> = images.filter { it.category == selectedCategory }
}
