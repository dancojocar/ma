package ro.cojocar.dan.portfolio.service

import com.google.gson.annotations.SerializedName

class LoginCredentials(
  @SerializedName("username") val username: String,
  @SerializedName("password") val password: String
)

class TokenHolder(@SerializedName("token") val token: String)