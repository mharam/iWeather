package com.takaapoo.weatherer.data.remote

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header


const val AstronomyBaseURL = "https://api.astronomyapi.com/api/"
const val applicationId = "612037ca-a836-4798-9d07-e195bc0d2bae"
const val applicationSecret = "36496755bfd5e71e3ac55e22170e055c06abaa07f054ddbef431d1c612d57953c9954d4ce72cc2daf1e92c08dd94270481afe78986e972edf77bd0604d85df16e974588f2f84f70c55febf4a5ea771cc5ec918f1130b85d97a372a7abe8679577a3311ad2fa198b7e75a413f41b64ee5"
const val credentials = "$applicationId:$applicationSecret"



interface AstronomyApiService {
    @GET("v2/bodies")
    suspend fun getBodies(@Header("Authorization") authorization: String): String
}

@Module
@InstallIn(ViewModelComponent::class)
object AstronomyApiModule {
    @Provides
    @ViewModelScoped
    fun provideAstronomyApiService(): AstronomyApiService {
        val retrofit = Retrofit.Builder()
            .addConverterFactory(ScalarsConverterFactory.create())
            .baseUrl(AstronomyBaseURL)
            .build()
        val astronomyApiService: AstronomyApiService by lazy {
            retrofit.create(AstronomyApiService::class.java)
        }
        return astronomyApiService
    }
}