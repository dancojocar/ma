package com.example.composemovieapp.movies.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.composemovieapp.movies.domain.Movie
import com.example.composemovieapp.movies.repo.Result
import com.example.composemovieapp.movies.usecase.GetMoviesUseCase
import com.example.composemovieapp.movies.usecase.AddMovieUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoviesViewModel @Inject constructor(
  val useCase: GetMoviesUseCase,
  val addMovieUseCase: AddMovieUseCase
) : ViewModel() {

  private val _uiState = MutableStateFlow(MoviesUiState())
  val uiState: StateFlow<MoviesUiState> = _uiState.asStateFlow()
  private var isDataLoaded = false

  fun loadInitialDataIfNeeded() {
    if (!isDataLoaded) {
      Log.d("MoviesViewModel", "Loading initial data")
      refreshAll()
      isDataLoaded = true
    }
  }

  private fun refreshAll() {
    viewModelScope.launch {
      try {
        _uiState.emit(uiState.value.copy(loading = true, error = false))

        when (val movieListResult = useCase()) {
          is Result.Error -> {
            Log.d("MoviesViewModel", "Error fetching movies: ${movieListResult.error}")
            _uiState.emit(
              uiState.value.copy(
                loading = false,
                error = true,
                errorMessage = "Error code: ${movieListResult.code} message: ${movieListResult.error}"
              )
            )
          }

          is Result.Success -> {
            Log.d("MoviesViewModel", "Fetched ${movieListResult.value.size} movies")
            _uiState.emit(
              uiState.value.copy(
                loading = false,
                movies = movieListResult.value
              )
            )
          }
        }
      } catch (e: Exception) {
        Log.e("MoviesViewModel", "Error in refreshAll", e)
        _uiState.emit(
          uiState.value.copy(
            loading = false,
            error = true,
            errorMessage = "Error refreshing movies: ${e.message}"
          )
        )
      }
    }
  }

  fun addMovie(movie: Movie, onSuccess: () -> Unit) {
    viewModelScope.launch {
      try {
        _uiState.update { it.copy(loading = true, error = false) }
        Log.d("MoviesViewModel", "Adding movie: $movie and now have ${uiState.value.movies} movies")

        when (val result = addMovieUseCase(movie)) {
          is Result.Success -> {
            _uiState.emit(
              uiState.value.copy(
                loading = false,
                movies = uiState.value.movies.plus(movie),
                error = false
              )
            )
            Log.d("MoviesViewModel", "Movie added successfully: ${_uiState.value.movies}")

            onSuccess()
          }

          is Result.Error -> {
            Log.e("MoviesViewModel", "Failed to add movie: ${result.error}")
            _uiState.emit(
              uiState.value.copy(
                loading = false,
                error = true,
                errorMessage = result.error ?: "Unknown error",
                fieldErrors = result.errorMap
              )
            )
          }
        }
      } catch (e: Exception) {
        Log.e("MoviesViewModel", "Exception in addMovie", e)
        _uiState.emit(
          uiState.value.copy(
            loading = false,
            error = true,
            errorMessage = "Error adding movie: ${e.message}",
            fieldErrors = emptyMap()
          )
        )
      }
    }
  }
}

data class MoviesUiState(
  val movies: List<Movie> = emptyList(),
  val error: Boolean = false,
  val errorMessage: String = "",
  val fieldErrors: Map<String, String> = emptyMap(),
  val loading: Boolean = false,
)

