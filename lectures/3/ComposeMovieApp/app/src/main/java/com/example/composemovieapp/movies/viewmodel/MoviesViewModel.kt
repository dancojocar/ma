package com.example.composemovieapp.movies.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.composemovieapp.movies.domain.Movie
import com.example.composemovieapp.movies.usecase.GetMoviesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoviesViewModel @Inject constructor(
    useCase: GetMoviesUseCase
) : ViewModel() {

    private val _listOfMovies: MutableState<List<Movie>> = mutableStateOf(emptyList())
    val listOfMovies: State<List<Movie>> = _listOfMovies

    init {
        viewModelScope.launch {
            val movieList = useCase()
            _listOfMovies.value = movieList
        }
    }

}
