package com.takaapoo.weatherer.di_modules

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.takaapoo.weatherer.data.remote.LandScapeApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton
import javax.net.ssl.SSLException

const val landScapeBaseURL_HTTPS = "https://takaapoo.com/api2/"
const val landScapeBaseURL_HTTP = "http://takaapoo.com/api2/"

@Module
@InstallIn(SingletonComponent::class)
object LandScapeApiModule {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    @Https
    @Provides
    @Singleton
    fun provideLandscapeApiServiceHttps(): LandScapeApiService {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .callTimeout(60, TimeUnit.SECONDS)
            .build()

        val retrofitHttps = Retrofit.Builder()
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .baseUrl(landScapeBaseURL_HTTPS)
            .client(okHttpClient)
            .build()
        val landScapeApiServiceHttps: LandScapeApiService by lazy {
            retrofitHttps.create(LandScapeApiService::class.java)
        }
        return landScapeApiServiceHttps
    }

    @Http
    @Provides
    @Singleton
    fun provideLandscapeApiServiceHttp(): LandScapeApiService {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .callTimeout(60, TimeUnit.SECONDS)
            .build()

        val retrofitHttp = Retrofit.Builder()
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .baseUrl(landScapeBaseURL_HTTP)
            .client(okHttpClient)
            .build()
        val landScapeApiServiceHttp: LandScapeApiService by lazy {
            retrofitHttp.create(LandScapeApiService::class.java)
        }
        return landScapeApiServiceHttp
    }
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Http

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Https

suspend fun <T> callWithFallback(
    httpsCall: suspend () -> T,
    httpCall: suspend () -> T
): T {
    return try {
        httpsCall()
    } catch (e: Exception) {
        // Only fallback for SSL or connection errors
        if (e is SSLException || e is ConnectException || e is SocketTimeoutException) {
            httpCall()
        } else {
            throw e
        }
    }
}