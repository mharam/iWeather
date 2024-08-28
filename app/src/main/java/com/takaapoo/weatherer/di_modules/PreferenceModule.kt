package com.takaapoo.weatherer.di_modules

import com.takaapoo.weatherer.data.repository.PreferenceRepositoryImpl
import com.takaapoo.weatherer.domain.repository.PreferenceRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
abstract class PreferenceModule {
    @Binds
    @ViewModelScoped
    abstract fun bindPreferenceRepository(
        preferenceRepositoryImpl: PreferenceRepositoryImpl
    ): PreferenceRepository
}