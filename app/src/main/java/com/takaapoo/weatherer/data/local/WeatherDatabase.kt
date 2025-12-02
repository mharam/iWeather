package com.takaapoo.weatherer.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters


@Database(
    entities = [Location::class, LocalHourlyWeather::class, LocalDailyWeather::class,
        LocalAirQuality::class, LocalHistoricHourlyWeather::class, LocalHistoricDailyWeather::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WeatherDatabase : RoomDatabase() {

    abstract fun weatherDao(): WeatherDao
    abstract fun locationDao(): LocationDao

    companion object {
        @Volatile
        private var INSTANCE: WeatherDatabase? = null

        fun getWeatherDatabase(context: Context): WeatherDatabase {
            return INSTANCE ?: synchronized(this){
                Room.databaseBuilder(context, WeatherDatabase::class.java, "weather_database")
                    .fallbackToDestructiveMigration(false)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}