package com.example.composemovieapp.util

import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException
import com.example.composemovieapp.movies.repo.Result

suspend fun <T> safeApiCall(dispatcher: CoroutineDispatcher, apiCall: suspend () -> T): Result<T> {
    return withContext(dispatcher) {
        try {
            Result.Success(apiCall.invoke())
        } catch (throwable: Throwable) {
            when (throwable) {
                is HttpException -> {
                    val code = throwable.code()
                    val errorBody = throwable.response()?.errorBody()?.string()
                    val errorMap = mutableMapOf<String, String>()

                    try {
                        // Parse the error body as JSON
                        val jsonObject = JSONObject(errorBody ?: "")
                        val errors = jsonObject.optJSONObject("errors")
                        if (errors != null) {
                            errors.keys().forEach { key ->
                                errorMap[key] = errors.getString(key)
                            }
                        }
                    } catch (e: Exception) {
                        // If JSON parsing fails, log the error
                        Log.e("safeApiCall", "Error parsing error response", e)
                    }

                    Result.Error(code, errorBody ?: throwable.message(), errorMap)
                }
                else -> {
                    Result.Error(-1, throwable.message)
                }
            }
        }
    }
} 