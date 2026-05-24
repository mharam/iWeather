package com.takaapoo.weatherer.di_modules

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Qualifier

@Module
@InstallIn(SingletonComponent::class)
object ClockModule {
    @SystemClock
    @Provides
    fun provideSystemClock(): Clock = Clock.systemDefaultZone()

    @UtcClock
    @Provides
    fun provideUtcClock(): Clock = Clock.systemUTC()
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SystemClock

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class UtcClock