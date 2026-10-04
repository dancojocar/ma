package com.unieats.app.di

import com.unieats.app.cloud.ConsentGatedCrashReporter
import com.unieats.app.cloud.CrashConsent
import com.unieats.app.cloud.CrashReporter
import com.unieats.app.cloud.FeatureFlags
import com.unieats.app.cloud.LogcatCrashReporter
import com.unieats.app.cloud.RemoteConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CloudModule {
    @Provides
    fun provideFeatureFlags(remoteConfig: RemoteConfig): FeatureFlags = remoteConfig

    @Provides
    @Singleton
    fun provideCrashReporter(consent: CrashConsent): CrashReporter =
        ConsentGatedCrashReporter(LogcatCrashReporter(), consent)
}
