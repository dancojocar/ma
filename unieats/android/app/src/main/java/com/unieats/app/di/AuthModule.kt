package com.unieats.app.di

import com.unieats.app.data.auth.AuthTokens
import com.unieats.app.data.auth.TokenStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {
    @Binds
    abstract fun bindAuthTokens(tokenStore: TokenStore): AuthTokens
}
