package com.example.composemovieapp.movies.usecase

import com.example.composemovieapp.movies.domain.Movie
import com.example.composemovieapp.movies.repo.MoviesRepository
import com.example.composemovieapp.movies.repo.Result
import javax.inject.Inject

interface GetMoviesUseCase {
    suspend operator fun invoke(): Result<List<Movie>>
}

class GetMoviesUseCaseImpl @Inject constructor(
    val repo: MoviesRepository
) : GetMoviesUseCase {
    override suspend fun invoke(): Result<List<Movie>> {
        return repo.getAllMovies()
    }
}