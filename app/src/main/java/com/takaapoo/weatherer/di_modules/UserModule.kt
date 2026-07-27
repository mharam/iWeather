package com.takaapoo.weatherer.di_modules

import com.takaapoo.weatherer.data.repository.UserRepositoryImpl
import com.takaapoo.weatherer.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UserModule {
    @Binds
    @Singleton
    abstract fun bindUserRepo(userRepositoryImpl: UserRepositoryImpl): UserRepository
}