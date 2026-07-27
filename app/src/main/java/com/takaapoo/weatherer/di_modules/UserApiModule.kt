package com.takaapoo.weatherer.di_modules

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.takaapoo.weatherer.data.remote.UserApiService
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

const val userBaseURL_HTTPS = "https://takaapoo.com/api2/"
const val userBaseURL_HTTP = "http://takaapoo.com/api2/"

@Module
@InstallIn(SingletonComponent::class)
object UserApiModule {
    private val json = Json {
        ignoreUnknownKeys = true
    }
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .callTimeout(60, TimeUnit.SECONDS)
        .build()

    @Https
    @Provides
    @Singleton
    fun provideUserApiServiceHttps(): UserApiService {
        val retrofitHttps = Retrofit.Builder()
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .baseUrl(userBaseURL_HTTPS)
            .client(okHttpClient)
            .build()
        val userApiServiceHttps: UserApiService by lazy {
            retrofitHttps.create(UserApiService::class.java)
        }
        return userApiServiceHttps
    }

    @Http
    @Provides
    @Singleton
    fun provideUserApiServiceHttp(): UserApiService {
        val retrofitHttps = Retrofit.Builder()
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .baseUrl(userBaseURL_HTTP)
            .client(okHttpClient)
            .build()
        val userApiServiceHttp: UserApiService by lazy {
            retrofitHttps.create(UserApiService::class.java)
        }
        return userApiServiceHttp
    }
}
