package com.example.movieapp.repo

import com.example.movieapp.service.RetrofitService

class NetworkRepository constructor(private val retrofitService: RetrofitService) {

    fun getAllMovies() = retrofitService.getAllMovies()
}
