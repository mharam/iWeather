package com.takaapoo.weatherer.domain.use_case

import android.util.Log
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import com.takaapoo.weatherer.domain.repository.AirQualityRepository
import com.takaapoo.weatherer.domain.repository.LocalWeatherRepository
import com.takaapoo.weatherer.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetHourlyChartDataUseCase @Inject constructor(
    private val locationRepository: LocationRepository,
    private val airQualityRepository: AirQualityRepository,
    private val localWeatherRepository: LocalWeatherRepository
){
    operator fun invoke(locationId: Int, startDate: String, endDate: String): Flow<List<HourlyChartDto>> {
        val hourlyWeatherFlow = localWeatherRepository.getLocationHourlyWeatherFlow(
            locationId, startDate, endDate
        )
        val dailyWeatherFlow = localWeatherRepository.getLocationDailyWeatherFlow(
            locationId, startDate, endDate
        )
        val airQualityFlow = airQualityRepository.getLocationAirQualityFlow(
            locationId, startDate, endDate
        )
        val locationFlow = locationRepository.getLocation(locationId)

        return combine(
            hourlyWeatherFlow,
            dailyWeatherFlow,
            airQualityFlow,
            locationFlow
        ){ hourlyData, dailyData, airQuality, location ->
            List(size = hourlyData.size){ index ->
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