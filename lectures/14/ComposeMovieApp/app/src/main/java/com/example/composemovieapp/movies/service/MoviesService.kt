package com.example.composemovieapp.movies.service

import com.example.composemovieapp.movies.domain.Movie
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Body

interface MoviesService {

    @GET("movies")
    suspend fun getAllMovies(): List<Movie>

    @POST("movie")
    suspend fun addMovie(@Body movie: Movie): Movie
}