package com.example.composemovieapp.movies.repo

import com.example.composemovieapp.di.AppModule
import com.example.composemovieapp.movies.domain.Movie
import com.example.composemovieapp.movies.service.MoviesService
import com.example.composemovieapp.util.safeApiCall
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

interface MoviesRepository {
    suspend fun getAllMovies(): Result<List<Movie>>
    suspend fun addMovie(movie: Movie): Result<Movie>
}

class MoviesRepositoryImpl @Inject constructor(
    private val service: MoviesService,
    @AppModule.IoDispatcher private val dispatcher: CoroutineDispatcher
) : MoviesRepository {
    override suspend fun getAllMovies(): Result<List<Movie>> {
        return safeApiCall(dispatcher) { service.getAllMovies() }
    }

    override suspend fun addMovie(movie: Movie): Result<Movie> {
        return safeApiCall(dispatcher) { service.addMovie(movie) }
    }
}
