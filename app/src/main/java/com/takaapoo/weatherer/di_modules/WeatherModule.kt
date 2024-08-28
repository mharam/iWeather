package com.takaapoo.weatherer.di_modules

import com.takaapoo.weatherer.data.repository.AirQualityRepositoryImpl
import com.takaapoo.weatherer.data.repository.RemoteWeatherRepositoryImpl
import com.takaapoo.weatherer.domain.repository.AirQualityRepository
import com.takaapoo.weatherer.domain.repository.RemoteWeatherRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WeatherModule {
    @Binds
    @Singleton
    abstract fun bindWeatherRepository(
        weatherRepositoryImpl: RemoteWeatherRepositoryImpl
    ): RemoteWeatherRepository
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AirQualityModule {
    @Binds
    @Singleton
    abstract fun bindAirQualityRepository(
        airQualityRepositoryImpl: AirQualityRepositoryImpl
    ): AirQualityRepository
}