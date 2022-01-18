package com.example.composemovieapp.movies.repo

import com.example.composemovieapp.movies.domain.Movie
import com.example.composemovieapp.movies.service.MoviesService
import javax.inject.Inject

interface MoviesRepository {
    suspend fun getAllMovies(): List<Movie>
}

class MoviesRepositoryImpl @Inject constructor(
    val service: MoviesService
) : MoviesRepository {
    override suspend fun getAllMovies(): List<Movie> {
        return service.getAllMovies()
    }
}