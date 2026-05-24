package com.takaapoo.weatherer.di_modules

import com.takaapoo.weatherer.data.repository.LandScapeRepositoryImpl
import com.takaapoo.weatherer.domain.repository.LandScapeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LandScapeModule {
    @Binds
    @Singleton
    abstract fun bindLandScapeRepository(
        landScapeRepositoryImpl: LandScapeRepositoryImpl
    ): LandScapeRepository
}