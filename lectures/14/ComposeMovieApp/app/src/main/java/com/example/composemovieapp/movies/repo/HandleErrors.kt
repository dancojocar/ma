package com.example.composemovieapp.movies.repo

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

sealed class Result<out T> {
  data class Success<out T>(val value: T) : Result<T>()
  data class Error(val code: Int? = null, val error: String? = null) : Result<Nothing>()
}

suspend fun <T> safeApiCall(dispatcher: CoroutineDispatcher, apiCall: suspend () -> T): Result<T> {
  return withContext(dispatcher) {
    try {
      Result.Success(apiCall.invoke())
    } catch (throwable: Throwable) {
      when (throwable) {
        is HttpException -> {
          val code = throwable.code()
          val errorResponse = throwable.message()
          Result.Error(code, errorResponse)
        }

        else -> {
          Result.Error(-1, throwable.message)
        }
      }
    }
  }
}