package com.example.composemovieapp.movies.service

import com.example.composemovieapp.movies.domain.Movie
import retrofit2.http.GET

interface MoviesService {

    @GET("movies")
    suspend fun getAllMovies(): List<Movie>

}