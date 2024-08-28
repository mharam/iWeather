package com.takaapoo.weatherer.di_modules

import com.takaapoo.weatherer.data.repository.TimeRepositoryImpl
import com.takaapoo.weatherer.domain.repository.TimeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class TimeModule {
    @Binds
    @Singleton
    abstract fun bindTimeRepository(
        timeRepositoryImpl: TimeRepositoryImpl
    ): TimeRepository
}