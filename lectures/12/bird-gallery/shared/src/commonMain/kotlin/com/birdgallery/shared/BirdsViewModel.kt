package com.birdgallery.shared

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class BirdImageDto(
    val author: String,
    val category: String,
    val path: String
)

class BirdsRepository {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun getImages(): List<BirdImage> {
        val response = httpClient.get("https://sebastianaigner.github.io/demo-image-api/pictures.json")
        val json = Json { ignoreUnknownKeys = true }
        val dtos = json.decodeFromString<List<BirdImageDto>>(response.bodyAsText())
        return dtos.map { BirdImage(it.author, it.category, it.path) }
    }

    fun close() {
        httpClient.close()
    }
}

class BirdsViewModel {
    private val repository = BirdsRepository()
    
    private val _uiState = MutableStateFlow(BirdsUiState())
    val uiState: StateFlow<BirdsUiState> = _uiState.asStateFlow()

    suspend fun loadImages() {
        try {
            val images = repository.getImages()
            _uiState.update { it.copy(images = images) }
        } catch (e: Exception) {
            println("Error loading images: ${e.message}")
        }
    }

    fun selectCategory(category: String) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onCleared() {
        repository.close()
    }
}
