package com.takaapoo.weatherer.di_modules

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.takaapoo.weatherer.data.remote.LocationTypeApiService
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

const val locationTypeBaseURL = "https://is-on-water.balbona.me/api/v1/get/"

@Module
@InstallIn(SingletonComponent::class)
object LocationTypeApiModule {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    @Provides
    @Singleton
    fun provideLocationTypeApiService(): LocationTypeApiService {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder()
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .baseUrl(locationTypeBaseURL)
            .client(okHttpClient)
            .build()
        val locationTypeApiService: LocationTypeApiService by lazy {
            retrofit.create(LocationTypeApiService::class.java)
        }
        return locationTypeApiService
    }
}