package com.takaapoo.weatherer.di_modules

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.takaapoo.weatherer.data.remote.LocationApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import javax.inject.Singleton

const val locationBaseURL = "https://api.geoapify.com/v1/geocode/"

@Module
@InstallIn(SingletonComponent::class)
object LocationApiModule {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    @Provides
    @Singleton
    fun provideTimeApiService(): LocationApiService {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .baseUrl(locationBaseURL)
            .build()
        val locationApiService: LocationApiService by lazy {
            retrofit.create(LocationApiService::class.java)
        }
        return locationApiService
    }
}