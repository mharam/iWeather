package com.takaapoo.weatherer.data.repository

import android.util.Base64
import com.takaapoo.weatherer.data.remote.AstronomyApiService
import com.takaapoo.weatherer.data.remote.credentials
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

interface AstronomyRepository {
    suspend fun getBodies(): String
}

class AstronomyRepositoryImpl @Inject constructor(
    private val astronomyApiService: AstronomyApiService
) : AstronomyRepository {
    private val encodedCredentials: String =
        Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)

    override suspend fun getBodies(): String {
        return astronomyApiService.getBodies("Basic $encodedCredentials")
    }
}

@Module
@InstallIn(ViewModelComponent::class)
abstract class AstronomyModule() {
    @Binds
    @ViewModelScoped
    abstract fun bindAstronomyRepository(
        astronomyRepositoryImpl: AstronomyRepositoryImpl
    ): AstronomyRepository
}