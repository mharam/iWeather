package com.takaapoo.weatherer.di_modules

import com.takaapoo.weatherer.data.repository.DataRefreshRepositoryImpl
import com.takaapoo.weatherer.domain.repository.DataRefreshRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
abstract class DataRefreshModule {
    @Binds
    @ViewModelScoped
    abstract fun bindDataRefreshRepository(
        dataRefreshRepositoryImpl: DataRefreshRepositoryImpl
    ): DataRefreshRepository
}