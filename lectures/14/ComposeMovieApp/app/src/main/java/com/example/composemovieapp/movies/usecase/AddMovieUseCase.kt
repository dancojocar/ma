package com.example.composemovieapp.movies.usecase

import com.example.composemovieapp.movies.domain.Movie
import com.example.composemovieapp.movies.repo.MoviesRepository
import com.example.composemovieapp.movies.repo.Result
import javax.inject.Inject

interface AddMovieUseCase {
  suspend operator fun invoke(movie: Movie): Result<Movie>
}

class AddMovieUseCaseImpl @Inject constructor(
  val repo: MoviesRepository
) : AddMovieUseCase {
  override suspend fun invoke(movie: Movie): Result<Movie> {
    return repo.addMovie(movie)
  }
}