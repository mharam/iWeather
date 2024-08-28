package com.takaapoo.weatherer.di_modules

import com.takaapoo.weatherer.data.local.LocationDao
import com.takaapoo.weatherer.data.local.WeatherDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object LocationDaoModule {
    @Provides
    fun provideLocationDaoService(weatherDatabase: WeatherDatabase): LocationDao {
        return weatherDatabase.locationDao()
    }
}