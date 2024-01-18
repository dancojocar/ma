package com.example.composemovieapp.movies.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.composemovieapp.movies.domain.Movie
import com.example.composemovieapp.movies.repo.Result
import com.example.composemovieapp.movies.usecase.GetMoviesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoviesViewModel @Inject constructor(
  val useCase: GetMoviesUseCase
) : ViewModel() {

  // UI state exposed to the UI
  private val _uiState = MutableStateFlow(MoviesUiState(loading = true))
  val uiState: StateFlow<MoviesUiState> = _uiState.asStateFlow()

  init {
    refreshAll()
  }

  private fun refreshAll() {
    _uiState.update { it.copy(loading = true) }
    viewModelScope.launch {

      when (val movieListResult = useCase()) {
        is Result.Error -> _uiState.update {
          it.copy(
            loading = false,
            error = true,
            errorMessage = "Error code: ${movieListResult.code} message: ${movieListResult.error}"
          )
        }

        is Result.Success -> _uiState.update {
          it.copy(
            loading = false,
            movies = movieListResult.value
          )
        }
      }
    }
  }
}

data class MoviesUiState(
  val movies: List<Movie> = emptyList(),
  val error: Boolean = false,
  val errorMessage: String = "",
  val loading: Boolean = false,
)

