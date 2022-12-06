package com.example.biometric.data

import com.example.biometric.data.model.Username
import com.example.biometric.data.model.Password
import com.example.biometric.data.model.UserToken
import com.example.biometric.data.remote.AuthApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoginRepository @Inject constructor(private val authApi: AuthApi) {

    suspend fun login(username: Username, password: Password): UserToken =
        authApi.login(username.value, password.value)
}