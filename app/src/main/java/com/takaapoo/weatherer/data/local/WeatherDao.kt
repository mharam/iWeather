package com.takaapoo.weatherer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import com.takaapoo.weatherer.domain.model.WeatherDto
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addHourlyWeather(hourlyWeathers: List<LocalHourlyWeather>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addDailyWeather(dailyWeathers: List<LocalDailyWeather>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addAirQuality(airQuality: List<LocalAirQuality>)

    @Query("SELECT * From locations LEFT JOIN hourly_weather ON " +
            "locations.id = hourly_weather.location_id")
    fun getAllHourlyWeather(): Flow<Map<Location, List<LocalHourlyWeather>>>

    @Query("SELECT * From hourly_weather WHERE location_id = :locationId " +
            "AND SUBSTR(hourly_weather.time, 1, 10) BETWEEN :startDate AND :endDate")
    fun getLocationHourlyWeatherFlow(locationId: Int, startDate: String, endDate: String):
            Flow<List<LocalHourlyWeather>>

    @Query("SELECT * From hourly_weather WHERE location_id = :locationId " +
            "AND SUBSTR(hourly_weather.time, 1, 10) BETWEEN :startDate AND :endDate")
    suspend fun getLocationHourlyWeather(locationId: Int, startDate: String, endDate: String):
            List<LocalHourlyWeather>

    @Query("SELECT * From hourly_weather WHERE location_id = :locationId " +
            "AND hourly_weather.time BETWEEN :startTime AND :endTime")
    suspend fun getLocationHourlyWeatherForTime(locationId: Int, startTime: String, endTime: String):
            List<LocalHourlyWeather>

    @Query("SELECT * From locations LEFT JOIN daily_weather ON locations.id = daily_weather.location_id " +
            "WHERE daily_weather.time = :time")
    suspend fun getDailyWeather(time: String): Map<Location, LocalDailyWeather>

    @Query("SELECT * From daily_weather WHERE location_id = :locationId " +
            "AND time BETWEEN :startDate AND :endDate")
    fun getLocationDailyWeatherFlow(locationId: Int, startDate: String, endDate: String):
            Flow<List<LocalDailyWeather>>

    @Query("SELECT * From daily_weather WHERE location_id = :locationId " +
            "AND time BETWEEN :startDate AND :endDate")
    suspend fun getLocationDailyWeather(locationId: Int, startDate: String, endDate: String):
            List<LocalDailyWeather>


    @Query("SELECT * From locations LEFT JOIN air_quality ON locations.id = air_quality.location_id")
    fun getAllAirQuality(): Flow<Map<Location, List<LocalAirQuality>>>

    @Query("SELECT * From air_quality WHERE location_id = :locationId " +
            "AND SUBSTR(air_quality.time, 1, 10) BETWEEN :startDate AND :endDate")
    fun getLocationAirQualityFlow(locationId: Int, startDate: String, endDate: String):
            Flow<List<LocalAirQuality>>

    @Query("SELECT * From air_quality WHERE location_id = :locationId " +
            "AND SUBSTR(air_quality.time, 1, 10) BETWEEN :startDate AND :endDate")
    suspend fun getLocationAirQuality(locationId: Int, startDate: String, endDate: String): List<LocalAirQuality>

    @Query("SELECT DISTINCT time From hourly_weather")
    fun getAllTimes(): Flow<List<String>>

    @Query("SELECT locations.id AS locationId , " +
            "locations.name AS locationName , " +
            "locations.utc_offset AS utcOffset , " +
            "hourly_weather.time AS time ," +
            "hourly_weather.temperature_2m AS currentTemperature , " +
            "hourly_weather.relativehumidity_2m AS currentHumidity , " +
            "hourly_weather.precipitation_probability AS currentPrecipitationProbability , " +
            "hourly_weather.weathercode AS currentWeatherCode , " +
            "hourly_weather.windspeed_10m AS currentWindSpeed , " +
            "hourly_weather.winddirection_10m AS currentWindDirection , " +
            "daily_weather.temperature_2m_max AS todayMaxTemperature , " +
            "daily_weather.temperature_2m_min AS todayMinTemperature , " +
            "daily_weather.sun_rise AS sunRise , " +
            "daily_weather.sun_set AS sunSet , " +
            "air_quality.us_aqi AS usAQI " +
            "From locations, hourly_weather, daily_weather LEFT JOIN air_quality " +
            "ON locations.id = air_quality.location_id AND hourly_weather.time = air_quality.time " +
            "WHERE locations.id = hourly_weather.location_id AND " +
            "locations.id = daily_weather.location_id AND " +
//            "locations.id = air_quality.location_id AND " +
            "SUBSTR(hourly_weather.time, 1, 10) = daily_weather.time AND " +
//            "hourly_weather.time = air_quality.time AND " +
            "SUBSTR(hourly_weather.time, 1, 10) BETWEEN :startDate AND :endDate " +
            "ORDER BY locations.custom_id, hourly_weather.time"
    )
    suspend fun getAllLocationsWeather(startDate: String, endDate: String) : List<WeatherDto>



}