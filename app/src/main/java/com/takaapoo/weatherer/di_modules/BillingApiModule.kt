package com.takaapoo.weatherer.di_modules

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.takaapoo.weatherer.data.remote.BillingApiService
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


@Module
@InstallIn(SingletonComponent::class)
object BillingApiModule {
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
    fun provideBillingApiServiceHttps(): BillingApiService {
        val retrofitHttps = Retrofit.Builder()
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .baseUrl(userBaseURL_HTTPS)
            .client(okHttpClient)
            .build()
        val billingApiServiceHttps: BillingApiService by lazy {
            retrofitHttps.create(BillingApiService::class.java)
        }
        return billingApiServiceHttps
    }

    @Http
    @Provides
    @Singleton
    fun provideBillingApiServiceHttp(): BillingApiService {
        val retrofitHttps = Retrofit.Builder()
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .baseUrl(userBaseURL_HTTP)
            .client(okHttpClient)
            .build()
        val billingApiServiceHttp: BillingApiService by lazy {
            retrofitHttps.create(BillingApiService::class.java)
        }
        return billingApiServiceHttp
    }
}
