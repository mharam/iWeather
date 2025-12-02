package com.takaapoo.weatherer.di_modules

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.takaapoo.weatherer.data.remote.AirQualityApiService
import com.takaapoo.weatherer.data.remote.HistoricWeatherApiService
import com.takaapoo.weatherer.data.remote.WeatherApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

const val weatherBaseURL = "https://api.open-meteo.com"
const val airQualityBaseURL = "https://air-quality-api.open-meteo.com/"
const val historicalWeatherBaseURL = "https://archive-api.open-meteo.com/"

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

    @Provides
    @Singleton
    fun provideHistoricWeatherApiService(): HistoricWeatherApiService {
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .baseUrl(historicalWeatherBaseURL)
            .client(client)
            .build()
        val historicalWeatherApiService: HistoricWeatherApiService by lazy {
            retrofit.create(HistoricWeatherApiService::class.java)
        }
        return historicalWeatherApiService
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