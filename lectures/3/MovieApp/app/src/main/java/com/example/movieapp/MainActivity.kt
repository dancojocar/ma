package com.example.movieapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.movieapp.databinding.ActivityMainBinding
import com.example.movieapp.repo.NetworkRepository
import com.example.movieapp.service.RetrofitService
import com.example.movieapp.viewmodel.MainViewModel
import com.example.movieapp.viewmodel.MyViewModelFactory
import logd
import loge

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val retrofitService = RetrofitService.getInstance()

        val viewModel =
            ViewModelProvider(this, MyViewModelFactory(NetworkRepository(retrofitService))).get(
                MainViewModel::class.java
            )

        val adapter = MainAdapter()

        binding.recyclerview.adapter = adapter

        viewModel.movieList.observe(this, Observer {
            logd("onCreate: $it")
            adapter.setMovieList(it)
        })

        viewModel.errorMessage.observe(this, Observer {
            loge("Error while getting the movies: $it")
        })
        viewModel.getAllMovies()
    }
}