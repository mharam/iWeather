package com.takaapoo.weatherer.di_modules

import com.takaapoo.weatherer.data.repository.LocalWeatherRepositoryImpl
import com.takaapoo.weatherer.domain.repository.LocalWeatherRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
abstract class LocalWeatherModule {
    @Binds
    @Singleton
    abstract fun bindLocalWeatherRepository(
        localWeatherRepositoryImpl: LocalWeatherRepositoryImpl
    ): LocalWeatherRepository
}