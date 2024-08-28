package com.takaapoo.weatherer.di_modules

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.takaapoo.weatherer.data.remote.TimeApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import javax.inject.Singleton

const val timeBaseURL = "https://timeapi.io/api/"

@Module
@InstallIn(SingletonComponent::class)
object TimeApiModule {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    @Provides
    @Singleton
    fun provideTimeApiService(): TimeApiService {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
//            .addConverterFactory(ScalarsConverterFactory.create())
//            .addConverterFactory(MoshiConverterFactory.create(moshi))
//            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .baseUrl(timeBaseURL)
            .build()
        val timeApiService: TimeApiService by lazy {
            retrofit.create(TimeApiService::class.java)
        }
        return timeApiService
    }
}