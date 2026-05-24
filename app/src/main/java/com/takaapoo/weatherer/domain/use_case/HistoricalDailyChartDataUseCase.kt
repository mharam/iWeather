package com.takaapoo.weatherer.domain.use_case

import android.util.Log
import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.data.remote.toLocalHistoricDailyWeather
import com.takaapoo.weatherer.di_modules.IoDispatcher
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.HistoricDailyChartDto
import com.takaapoo.weatherer.domain.repository.LocalWeatherRepository
import com.takaapoo.weatherer.domain.repository.RemoteWeatherRepository
import com.takaapoo.weatherer.ui.screens.detail.DailyWeatherQuantity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.withContext
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.floor

class HistoricalDailyChartDataUseCase @Inject constructor(
    private val localWeatherRepository: LocalWeatherRepository,
    private val remoteWeatherRepository: RemoteWeatherRepository,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(
        location: Location,
        xAxisStart: Float,
        xAxisEnd: Float,
        weatherQuantity: DailyWeatherQuantity,
        appSettings: AppSettings,
        flowCollector: FlowCollector<MyResult<*>>
    ) {
        val xAxisLength = xAxisEnd - xAxisStart
        val xAxisMiddle = xAxisStart + xAxisLength / 2
        val extendedStart = xAxisMiddle - xAxisLength.coerceAtLeast(15f)
        val extendedEnd = xAxisMiddle + xAxisLength.coerceAtLeast(15f)

        val realStartDate = LocalDate.now().plusDays(floor(xAxisStart).toLong()).toString()
        val realEndDate = LocalDate.now().plusDays(ceil(xAxisEnd).toLong()).toString()

        val startDate = LocalDate.now().plusDays(floor(extendedStart).toLong())
            .coerceAtMost(LocalDate.now().minusDays(4)).toString()
        val endDate = LocalDate.now().plusDays(ceil(extendedEnd).toLong())
            .coerceAtMost(LocalDate.now().minusDays(3)).toString()

        withContext(ioDispatcher) {
            val dailyData = localWeatherRepository.getLocationHistoricDailyWeather(
                locationId = location.id,
                startDate = startDate,
                endDate = endDate
            )
            val fromIndex = dailyData.indexOfFirst { it.time == realStartDate }
            val toIndex = dailyData.indexOfLast { it.time == realEndDate }
            val dataAlreadyExist = fromIndex > -1 && toIndex > -1 &&
                    !dailyData.subList(fromIndex, toIndex).map { it.quantityValue(weatherQuantity) }.contains(null)

            if (dataAlreadyExist) {
                flowCollector.emit(
                    MyResult.Success(
                        dailyData.map {
                            HistoricDailyChartDto(
                                dailyWeather = it.applyUnits(appSettings),
                                weatherQuantity = weatherQuantity
                            )
                        }
                    )
                )
            } else {
                flowCollector.emit(MyResult.Loading())
                delay(100)
                val dailyDataResult =
                    remoteWeatherRepository.getHistoricDailyWeatherFromServer(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        startDate = startDate,
                        endDate = endDate,
                        weatherQuantity = weatherQuantity
                    )

                if (dailyDataResult is MyResult.Success) {
                    val data = dailyDataResult.data
                    val localHistoricDailyWeather = data.dailyWeather.toLocalHistoricDailyWeather(location.id)
                    localWeatherRepository.insertHistoricDailyColumn(weatherQuantity, localHistoricDailyWeather)

                    flowCollector.emit(
                        MyResult.Success(
                            localHistoricDailyWeather.map {
                                HistoricDailyChartDto(
                                    dailyWeather = it,
                                    weatherQuantity = weatherQuantity
                                )
                            }
                        )
                    )
                } else {
                    flowCollector.emit(
                        MyResult.Error(
                            (dailyDataResult as MyResult.Error).let { it.exception ?: Exception(it.message) }
                        ).also {
                            Log.i("test1", "${it.exception ?: Exception(it.message)}")
                        }
                    )
                }
            }
        }
    }
}