package com.takaapoo.weatherer.domain.use_case

import android.util.Log
import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.data.local.accumulate
import com.takaapoo.weatherer.data.remote.toLocalHistoricHourlyWeather
import com.takaapoo.weatherer.di_modules.IoDispatcher
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.HistoricHourlyChartDto
import com.takaapoo.weatherer.domain.repository.LocalWeatherRepository
import com.takaapoo.weatherer.domain.repository.RemoteWeatherRepository
import com.takaapoo.weatherer.ui.screens.detail.HourlyWeatherQuantity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.floor

class HistoricalHourlyChartDataUseCase @Inject constructor(
    private val localWeatherRepository: LocalWeatherRepository,
    private val remoteWeatherRepository: RemoteWeatherRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
){
    suspend operator fun invoke(
        location: Location,
        xAxisStart: Float,
        xAxisEnd: Float,
        weatherQuantities: List<HourlyWeatherQuantity>,
//        zone: ZoneId,
        appSettings: AppSettings,
        flowCollector: FlowCollector<MyResult<*>>
    ) {
        val xAxisLength = xAxisEnd - xAxisStart
        val xAxisMiddle = xAxisStart + xAxisLength / 2
        val extendedStart = xAxisMiddle - xAxisLength.coerceAtLeast(15 * 24f)
        val extendedEnd = xAxisMiddle + xAxisLength.coerceAtLeast(15 * 24f)

        val realStartDate = LocalDate.now().plusDays(floor(xAxisStart / 24).toLong()).toString()
        val realEndDate = LocalDate.now().plusDays(ceil(xAxisEnd / 24).toLong()).toString()

        val startDate = LocalDate.now().plusDays(floor(extendedStart / 24).toLong())
            .coerceAtMost(LocalDate.now().minusDays(4)).toString()
        val endDate = LocalDate.now().plusDays(ceil(extendedEnd / 24).toLong())
            .coerceAtMost(LocalDate.now().minusDays(3)).toString()

        withContext(ioDispatcher) {
            val hourlyData = localWeatherRepository.getLocationHistoricHourlyWeather(
                locationId = location.id,
                startDate = startDate,
                endDate = endDate
            )
            val notLoadedQuantities = weatherQuantities.mapNotNull { quantity ->
                val fromIndex = hourlyData.indexOfFirst { it.time.substring(0 .. 9) == realStartDate }
                val toIndex = hourlyData.indexOfLast { it.time.substring(0 .. 9) == realEndDate }
                if (fromIndex == -1 || toIndex == -1) quantity
                else {
                    if (hourlyData.subList(fromIndex, toIndex).map { it.quantityValue(quantity) }
                        .contains(null)) quantity
                    else null
                }
            }
//            Log.i("test1", "row count = ${localWeatherRepository.getRowCounts()}")
//            Log.i("test1", "Loading ... startDate = $startDate , endDate = $endDate , realStartDate = $realStartDate ,  realEndDate = $realEndDate , notLoadedQuantities = $notLoadedQuantities")
            if (notLoadedQuantities.isEmpty()) {
                flowCollector.emit(
                    MyResult.Success(
                        hourlyData.map {
                            HistoricHourlyChartDto(
                                hourlyWeather = it.applyUnits(appSettings),
                                locationName = location.name,
                                utcOffset = location.utcOffset,
                                weatherQuantities = weatherQuantities
                            )
                        }
                    )
                )
            } else {
                flowCollector.emit(MyResult.Loading())
                delay(100)
                val hourlyDataResult =
                    remoteWeatherRepository.getHistoricHourlyWeatherFromServer(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        startDate = startDate,
                        endDate = endDate,
                        weatherQuantities = notLoadedQuantities
                    )

                if (hourlyDataResult is MyResult.Success) {
                    val data = hourlyDataResult.data
                    val localHistoricHourlyWeather = data.hourlyWeather.toLocalHistoricHourlyWeather(location.id)
                    notLoadedQuantities.forEach { quantity ->
                        localWeatherRepository.insertHistoricHourlyColumn(quantity, localHistoricHourlyWeather)
                    }

//                    Log.i("test1", "data size = ${data.hourlyWeather.time.size}")
                    flowCollector.emit(
                        MyResult.Success(
                            localHistoricHourlyWeather.accumulate(hourlyData).map {
                                HistoricHourlyChartDto(
                                    hourlyWeather = it.applyUnits(appSettings),
                                    locationName = location.name,
                                    utcOffset = location.utcOffset,
                                    weatherQuantities = weatherQuantities
                                )
                            }
                        )
                    )
                } else {
                    flowCollector.emit(
                        MyResult.Error(
                            (hourlyDataResult as MyResult.Error).let { it.exception ?: Exception(it.message) }
                        ).also {
                            Log.i("test1", "${it.exception ?: Exception(it.message)}")
                        }
                    )
                }
            }
        }
    }

}