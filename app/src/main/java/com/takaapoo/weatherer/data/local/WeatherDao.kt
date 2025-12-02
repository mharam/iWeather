package com.takaapoo.weatherer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.takaapoo.weatherer.data.local.SingleQuantityDailyWeather.CloudCoverDailyWeather
import com.takaapoo.weatherer.data.local.SingleQuantityDailyWeather.HumidityDailyWeather
import com.takaapoo.weatherer.data.local.SingleQuantityDailyWeather.PrecipitationDailyWeather
import com.takaapoo.weatherer.data.local.SingleQuantityDailyWeather.TemperatureMeanDailyWeather
import com.takaapoo.weatherer.data.local.SingleQuantityDailyWeather.TemperatureMinMaxDailyWeather
import com.takaapoo.weatherer.data.local.SingleQuantityDailyWeather.WindGustsDailyWeather
import com.takaapoo.weatherer.data.local.SingleQuantityDailyWeather.WindSpeedDailyWeather
import com.takaapoo.weatherer.data.local.SingleQuantityHourlyWeather.ApparentTemperatureHourlyWeather
import com.takaapoo.weatherer.data.local.SingleQuantityHourlyWeather.CloudCoverHourlyWeather
import com.takaapoo.weatherer.data.local.SingleQuantityHourlyWeather.DewPointHourlyWeather
import com.takaapoo.weatherer.data.local.SingleQuantityHourlyWeather.HumidityHourlyWeather
import com.takaapoo.weatherer.data.local.SingleQuantityHourlyWeather.IsDayHourlyWeather
import com.takaapoo.weatherer.data.local.SingleQuantityHourlyWeather.PrecipitationHourlyWeather
import com.takaapoo.weatherer.data.local.SingleQuantityHourlyWeather.RainHourlyWeather
import com.takaapoo.weatherer.data.local.SingleQuantityHourlyWeather.SnowfallHourlyWeather
import com.takaapoo.weatherer.data.local.SingleQuantityHourlyWeather.SurfacePressureHourlyWeather
import com.takaapoo.weatherer.data.local.SingleQuantityHourlyWeather.TemperatureHourlyWeather
import com.takaapoo.weatherer.data.local.SingleQuantityHourlyWeather.WeatherCodeHourlyWeather
import com.takaapoo.weatherer.data.local.SingleQuantityHourlyWeather.WindDirectionHourlyWeather
import com.takaapoo.weatherer.data.local.SingleQuantityHourlyWeather.WindSpeedHourlyWeather
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

    @Query("SELECT * From locations LEFT JOIN hourly_weather ON locations.id = hourly_weather.location_id " +
            "WHERE SUBSTR(hourly_weather.time, 1, 10) BETWEEN :startDate AND :endDate")
    suspend fun getHourlyWeather(startDate: String, endDate: String): Map<Location, List<LocalHourlyWeather>>

    @Query("SELECT * From locations LEFT JOIN air_quality ON locations.id = air_quality.location_id " +
            "WHERE SUBSTR(air_quality.time, 1, 10) BETWEEN :startDate AND :endDate")
    suspend fun getHourlyAirQuality(startDate: String, endDate: String): Map<Location, List<LocalAirQuality>>

    @Query("SELECT * From hourly_weather WHERE location_id = :locationId " +
            "AND hourly_weather.time BETWEEN :startTime AND :endTime")
    suspend fun getLocationHourlyWeatherForTime(locationId: Int, startTime: String, endTime: String):
            List<LocalHourlyWeather>

    @Query("SELECT * From locations LEFT JOIN daily_weather ON locations.id = daily_weather.location_id " +
            "WHERE daily_weather.time BETWEEN :startDate AND :endDate")
    suspend fun getDailyWeather(startDate: String, endDate: String): Map<Location, List<LocalDailyWeather>>

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

/*
    @Query("SELECT locations.id AS locationId , " +
            "locations.name AS locationName , " +
            "locations.latitude AS latitude , " +
            "locations.longitude AS longitude , " +
            "locations.utc_offset AS utcOffset , " +
            "hourly_weather.* , " +
            "air_quality.* , " +

//            "hourly_weather.time AS time ," +
//            "hourly_weather.temperature_2m AS currentTemperature , " +
//            "hourly_weather.relativehumidity_2m AS currentHumidity , " +
//            "hourly_weather.precipitation_probability AS currentPrecipitationProbability , " +
//            "hourly_weather.weathercode AS currentWeatherCode , " +
//            "hourly_weather.windspeed_10m AS currentWindSpeed , " +
//            "hourly_weather.winddirection_10m AS currentWindDirection , " +

            "daily_weather.temperature_2m_max AS todayMaxTemperature , " +
            "daily_weather.temperature_2m_min AS todayMinTemperature , " +
            "daily_weather.sun_rise AS sunRise , " +
            "daily_weather.sun_set AS sunSet " +
//            "air_quality.us_aqi AS usAQI " +
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
*/

    @Query("SELECT * From historical_hourly_weather WHERE location_id = :locationId " +
            "AND SUBSTR(time, 1, 10) BETWEEN :startDate AND :endDate ORDER BY time")
    suspend fun getLocationHistoricHourlyWeather(locationId: Int, startDate: String, endDate: String):
            List<LocalHistoricHourlyWeather>

    @Query("SELECT COUNT(*) From historical_hourly_weather")
    suspend fun getRowCounts(): Long

    @Transaction
    suspend fun insertColumn(data: List<SingleQuantityHourlyWeather>) {
        if (data.isEmpty()) return
        when (data[0]) {
            is TemperatureHourlyWeather -> insertTemperature(data as List<TemperatureHourlyWeather>)
            is HumidityHourlyWeather -> insertHumidity(data as List<HumidityHourlyWeather>)
            is ApparentTemperatureHourlyWeather -> insertApparentTemperature(data as List<ApparentTemperatureHourlyWeather>)
            is CloudCoverHourlyWeather -> insertCloudCover(data as List<CloudCoverHourlyWeather>)
            is DewPointHourlyWeather -> insertDewPoint(data as List<DewPointHourlyWeather>)
            is IsDayHourlyWeather -> insertIsDay(data as List<IsDayHourlyWeather>)
            is PrecipitationHourlyWeather -> insertPrecipitation(data as List<PrecipitationHourlyWeather>)
            is RainHourlyWeather -> insertRain(data as List<RainHourlyWeather>)
            is SnowfallHourlyWeather -> insertSnowfall(data as List<SnowfallHourlyWeather>)
            is SurfacePressureHourlyWeather -> insertSurfacePressure(data as List<SurfacePressureHourlyWeather>)
            is WeatherCodeHourlyWeather -> insertWeatherCode(data as List<WeatherCodeHourlyWeather>)
            is WindDirectionHourlyWeather -> insertWindDirection(data as List<WindDirectionHourlyWeather>)
            is WindSpeedHourlyWeather -> insertWindSpeed(data as List<WindSpeedHourlyWeather>)
        }
        trimTable(100_000)
    }

    @Query("DELETE FROM historical_hourly_weather WHERE time_stamp NOT IN " +
            "(SELECT time_stamp FROM historical_hourly_weather ORDER BY time_stamp DESC LIMIT :maxRows)")
    suspend fun trimTable(maxRows: Int)

    /*@Query("""
        INSERT INTO historical_hourly_weather (location_id, time, temperature_2m)
        VALUES(:temperatures)
    """)
    fun insertTemperatures(temperatures: List<TemperatureHourlyWeather>)*/

    @Upsert(entity = LocalHistoricHourlyWeather::class)
    fun insertTemperature(temperatures: List<TemperatureHourlyWeather>)

    @Upsert(entity = LocalHistoricHourlyWeather::class)
    fun insertHumidity(humidities: List<HumidityHourlyWeather>)

    @Upsert(entity = LocalHistoricHourlyWeather::class)
    fun insertDewPoint(dewPoints: List<DewPointHourlyWeather>)

    @Upsert(entity = LocalHistoricHourlyWeather::class)
    fun insertApparentTemperature(temperatures: List<ApparentTemperatureHourlyWeather>)

    @Upsert(entity = LocalHistoricHourlyWeather::class)
    fun insertPrecipitation(precipitations: List<PrecipitationHourlyWeather>)

    @Upsert(entity = LocalHistoricHourlyWeather::class)
    fun insertRain(rains: List<RainHourlyWeather>)

    @Upsert(entity = LocalHistoricHourlyWeather::class)
    fun insertSnowfall(snowfalls: List<SnowfallHourlyWeather>)

    @Upsert(entity = LocalHistoricHourlyWeather::class)
    fun insertWeatherCode(codes: List<WeatherCodeHourlyWeather>)

    @Upsert(entity = LocalHistoricHourlyWeather::class)
    fun insertCloudCover(cloudCovers: List<CloudCoverHourlyWeather>)

    @Upsert(entity = LocalHistoricHourlyWeather::class)
    fun insertSurfacePressure(pressures: List<SurfacePressureHourlyWeather>)

    @Upsert(entity = LocalHistoricHourlyWeather::class)
    fun insertWindSpeed(speeds: List<WindSpeedHourlyWeather>)

    @Upsert(entity = LocalHistoricHourlyWeather::class)
    fun insertWindDirection(directions: List<WindDirectionHourlyWeather>)

    @Upsert(entity = LocalHistoricHourlyWeather::class)
    fun insertIsDay(isDays: List<IsDayHourlyWeather>)

    @Query("DELETE From historical_hourly_weather")
    suspend fun deleteHistoricHourlyWeather()


    @Query("SELECT * From historical_daily_weather WHERE location_id = :locationId " +
            "AND time BETWEEN :startDate AND :endDate ORDER BY time")
    suspend fun getLocationHistoricDailyWeather(locationId: Int, startDate: String, endDate: String):
            List<LocalHistoricDailyWeather>

    @Transaction
    suspend fun insertDailyColumn(data: List<SingleQuantityDailyWeather>) {
        if (data.isEmpty()) return
        when (data[0]) {
            is TemperatureMinMaxDailyWeather -> insertDailyTemperature(data as List<TemperatureMinMaxDailyWeather>)
            is CloudCoverDailyWeather -> insertDailyCloudCover(data as List<CloudCoverDailyWeather>)
            is HumidityDailyWeather -> insertDailyHumidity(data as List<HumidityDailyWeather>)
            is PrecipitationDailyWeather -> insertDailyPrecipitation(data as List<PrecipitationDailyWeather>)
            is TemperatureMeanDailyWeather -> insertDailyTemperatureMean(data as List<TemperatureMeanDailyWeather>)
            is WindGustsDailyWeather -> insertDailyWindGusts(data as List<WindGustsDailyWeather>)
            is WindSpeedDailyWeather -> insertDailyWindSpeed(data as List<WindSpeedDailyWeather>)
        }
        trimDailyTable(100_000)
    }

    @Query("DELETE FROM historical_daily_weather WHERE time_stamp NOT IN " +
            "(SELECT time_stamp FROM historical_daily_weather ORDER BY time_stamp DESC LIMIT :maxRows)")
    suspend fun trimDailyTable(maxRows: Int)

    @Upsert(entity = LocalHistoricDailyWeather::class)
    fun insertDailyTemperature(temperatures: List<TemperatureMinMaxDailyWeather>)

    @Upsert(entity = LocalHistoricDailyWeather::class)
    fun insertDailyTemperatureMean(temperatures: List<TemperatureMeanDailyWeather>)

    @Upsert(entity = LocalHistoricDailyWeather::class)
    fun insertDailyCloudCover(temperatures: List<CloudCoverDailyWeather>)

    @Upsert(entity = LocalHistoricDailyWeather::class)
    fun insertDailyHumidity(temperatures: List<HumidityDailyWeather>)

    @Upsert(entity = LocalHistoricDailyWeather::class)
    fun insertDailyPrecipitation(temperatures: List<PrecipitationDailyWeather>)

    @Upsert(entity = LocalHistoricDailyWeather::class)
    fun insertDailyWindSpeed(temperatures: List<WindSpeedDailyWeather>)

    @Upsert(entity = LocalHistoricDailyWeather::class)
    fun insertDailyWindGusts(temperatures: List<WindGustsDailyWeather>)
}