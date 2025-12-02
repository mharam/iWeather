package com.takaapoo.weatherer.domain.use_case

import com.takaapoo.weatherer.data.local.LocalAirQuality
import com.takaapoo.weatherer.data.local.LocalDailyWeather
import com.takaapoo.weatherer.data.local.LocalHourlyWeather
import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import com.takaapoo.weatherer.domain.repository.AirQualityRepository
import com.takaapoo.weatherer.domain.repository.LocalWeatherRepository
import com.takaapoo.weatherer.domain.repository.LocationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.max

class GetHourlyChartDataUseCase @Inject constructor(
    private val locationRepository: LocationRepository,
    private val airQualityRepository: AirQualityRepository,
    private val localWeatherRepository: LocalWeatherRepository
){
    suspend operator fun invoke(
        locationId: Int,
        startDate: String,
        endDate: String,
        appSettings: AppSettings
    ): List<HourlyChartDto> {
        return withContext(Dispatchers.IO) {
            val hourlyDataDeferred = async {
                localWeatherRepository.getLocationHourlyWeather(locationId, startDate, endDate, appSettings)
            }
            val dailyDataDeferred = async {
                localWeatherRepository.getLocationDailyWeather(locationId, startDate, endDate, appSettings)
            }
            val airQualityDeferred = async {
                airQualityRepository.getLocationAirQuality(locationId, startDate, endDate)
            }
            val locationDeferred = async {
                locationRepository.getLocation(locationId)
            }
            val data = awaitAll(hourlyDataDeferred, dailyDataDeferred, airQualityDeferred, locationDeferred)
            val hourlyData = data[0] as? List<LocalHourlyWeather> ?: emptyList()
            val dailyData = data[1] as? List<LocalDailyWeather> ?: emptyList()
            val airQuality = data[2] as? List<LocalAirQuality> ?: emptyList()
            val location = data[3] as? Location

            if ((hourlyData.isEmpty() && dailyData.isEmpty() && airQuality.isEmpty()) || location == null)
                emptyList()
            else {
                val size = max(hourlyData.size, airQuality.size)
                List(size = size) { index ->
                    val dailyIndex = index / 24
                    HourlyChartDto(
                        hourlyWeather = hourlyData.getOrNull(index),
                        airQuality = airQuality.getOrNull(index),
                        locationName = location.name,
                        utcOffset = location.utcOffset,
                        maxTemperature = dailyData.getOrNull(dailyIndex)?.temperatureMax,
                        minTemperature = dailyData.getOrNull(dailyIndex)?.temperatureMin,
                        sunRise = dailyData.getOrNull(dailyIndex)?.sunRise,
                        sunSet = dailyData.getOrNull(dailyIndex)?.sunSet
                    )
                }
            }
        }
    }

}