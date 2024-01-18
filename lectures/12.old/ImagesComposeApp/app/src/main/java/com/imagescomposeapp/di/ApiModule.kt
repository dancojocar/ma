package com.imagescomposeapp.di

import com.imagescomposeapp.api.ImagesRepository
import com.imagescomposeapp.api.impl.ImagesRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
object ApiModule {

    @Provides
    fun provideImagesRepository(): ImagesRepository =
        ImagesRepositoryImpl()

}