package com.takaapoo.weatherer.di_modules

import android.content.Context
import com.takaapoo.weatherer.data.local.WeatherDao
import com.takaapoo.weatherer.data.local.WeatherDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WeatherDaoModule {

    @Provides
    fun provideWeatherDaoService(weatherDatabase: WeatherDatabase): WeatherDao {
        return weatherDatabase.weatherDao()
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WeatherDatabase {
        return WeatherDatabase.getWeatherDatabase(context)
    }
}