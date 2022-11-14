package com.imagescomposeapp.api

interface ImagesRepository {

    suspend fun getImage(imageId: Int?): ImageData?

    suspend fun getImages(): List<ImageData>

    suspend fun likeImage(imageId: Int): Result<ImageData>

    suspend fun searchImages(query: String): List<ImageData>
}
