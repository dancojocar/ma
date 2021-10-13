package com.example.composemovieapp.movies.usecase

import com.example.composemovieapp.movies.domain.Movie
import com.example.composemovieapp.movies.repo.MoviesRepository
import javax.inject.Inject

interface GetMoviesUseCase {
    suspend operator fun invoke(): List<Movie>
}

class GetMoviesUseCaseImpl @Inject constructor(
    val repo: MoviesRepository
) : GetMoviesUseCase {
    override suspend fun invoke(): List<Movie> {
        return repo.getAllMovies()
    }
}