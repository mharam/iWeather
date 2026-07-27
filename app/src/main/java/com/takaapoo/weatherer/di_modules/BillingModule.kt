package com.takaapoo.weatherer.di_modules

import com.takaapoo.weatherer.data.repository.BillingRepositoryImpl
import com.takaapoo.weatherer.domain.repository.BillingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BillingModule {
    @Binds
    @Singleton
    abstract fun bindBillingRepo(billingRepositoryImpl: BillingRepositoryImpl): BillingRepository
}