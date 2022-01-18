package com.example.composemovieapp.movies.domain

data class Movie(
    val category: String,
    val desc: String,
    val imageUrl: String,
    val link: String,
    val name: String
)