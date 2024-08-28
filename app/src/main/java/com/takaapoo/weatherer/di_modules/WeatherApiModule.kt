package com.takaapoo.weatherer.di_modules

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.takaapoo.weatherer.data.remote.AirQualityApiService
import com.takaapoo.weatherer.data.remote.WeatherApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import javax.inject.Singleton

const val weatherBaseURL = "https://api.open-meteo.com"
const val airQualityBaseURL = "https://air-quality-api.open-meteo.com/"

@Module
@InstallIn(SingletonComponent::class)
object WeatherApiModule {
    private val json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideWeatherApiService(): WeatherApiService {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .baseUrl(weatherBaseURL)
            .build()
        val weatherApiService: WeatherApiService by lazy {
            retrofit.create(WeatherApiService::class.java)
        }
        return weatherApiService
    }
}

@Module
@InstallIn(SingletonComponent::class)
object AirQualityApiModule {
    private val json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideAirQualityApiService(): AirQualityApiService {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .baseUrl(airQualityBaseURL)
            .build()
        val airQualityApiService: AirQualityApiService by lazy {
            retrofit.create(AirQualityApiService::class.java)
        }
        return airQualityApiService
    }
}