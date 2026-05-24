package com.takaapoo.weatherer.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.takaapoo.weatherer.utility.toFloatOrNull
import com.takaapoo.weatherer.utility.toIntOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.io.InputStream

@RunWith(AndroidJUnit4::class)
class WeatherDaoTest {
    private val dispatcher = StandardTestDispatcher()
    private lateinit var weatherDao: WeatherDao
    private lateinit var locationDao: LocationDao
    private lateinit var context: Context

    private lateinit var db: WeatherDatabase
    val myLocation = Location(
        id = 34,
        customId = 5,
        name = "Tehran"
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        context = InstrumentationRegistry.getInstrumentation().context

        Dispatchers.setMain(dispatcher)
        db = Room.inMemoryDatabaseBuilder(context, WeatherDatabase::class.java).build()
        weatherDao = db.weatherDao()
        locationDao = db.locationDao()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @After
    @Throws(IOException::class)
    fun tearDown() {
        db.close()
        Dispatchers.resetMain()
    }

    private fun loadCsvFromResources(fileName: String): List<List<String>> {
        // Load file from test/resources
        val inputStream: InputStream = context.assets.open(fileName)

        // Parse CSV (using manual parser or library)
        return CsvParser.parseCsv(inputStream, hasHeader = true)
    }
    private fun parseCsvToLocalHourlyWeather(csvRows: List<List<String>>): List<LocalHourlyWeather> {
        return csvRows.mapNotNull { row ->
            try {
                // Map CSV columns to LocalHourlyWeather properties
                // Adjust indices based on your CSV structure
                LocalHourlyWeather(
                    locationId = row[0].toInt(),
                    time = row[1],
                    temperature = row[2].toFloatOrNull(),
                    temperatureControl1X = row[3].toFloatOrNull(),
                    temperatureControl1Y = row[4].toFloatOrNull(),
                    temperatureControl2X = row[5].toFloatOrNull(),
                    temperatureControl2Y = row[6].toFloatOrNull(),
                    humidity = row[7].toFloatOrNull(),
                    humidityControl1X = row[8].toFloatOrNull(),
                    humidityControl1Y = row[9].toFloatOrNull(),
                    humidityControl2X = row[10].toFloatOrNull(),
                    humidityControl2Y = row[11].toFloatOrNull(),
                    dewPoint = row[12].toFloatOrNull(),
                    dewPointControl1X = row[13].toFloatOrNull(),
                    dewPointControl1Y = row[14].toFloatOrNull(),
                    dewPointControl2X = row[15].toFloatOrNull(),
                    dewPointControl2Y = row[16].toFloatOrNull(),
                    apparentTemperature = row[17].toFloatOrNull(),
                    apparentTemperatureControl1X = row[18].toFloatOrNull(),
                    apparentTemperatureControl1Y = row[19].toFloatOrNull(),
                    apparentTemperatureControl2X = row[20].toFloatOrNull(),
                    apparentTemperatureControl2Y = row[21].toFloatOrNull(),
                    precipitationProbability = row[22].toFloatOrNull(),
                    precipitationProbabilityControl1X = row[23].toFloatOrNull(),
                    precipitationProbabilityControl1Y = row[24].toFloatOrNull(),
                    precipitationProbabilityControl2X = row[25].toFloatOrNull(),
                    precipitationProbabilityControl2Y = row[26].toFloatOrNull(),
                    precipitation = row[27].toFloatOrNull(),
                    rain = row[28].toFloatOrNull(),
                    showers = row[29].toFloatOrNull(),
                    snowfall = row[30].toFloatOrNull(),
                    weatherCode = row[31].toIntOrNull(),
                    cloudCover = row[32].toFloatOrNull(),
                    cloudCoverControl1X = row[33].toFloatOrNull(),
                    cloudCoverControl1Y = row[34].toFloatOrNull(),
                    cloudCoverControl2X = row[35].toFloatOrNull(),
                    cloudCoverControl2Y = row[36].toFloatOrNull(),
                    surfacePressure = row[37].toFloatOrNull(),
                    surfacePressureControl1X = row[38].toFloatOrNull(),
                    surfacePressureControl1Y = row[39].toFloatOrNull(),
                    surfacePressureControl2X = row[40].toFloatOrNull(),
                    surfacePressureControl2Y = row[41].toFloatOrNull(),
                    visibility = row[42].toFloatOrNull(),
                    visibilityControl1X = row[43].toFloatOrNull(),
                    visibilityControl1Y = row[44].toFloatOrNull(),
                    visibilityControl2X = row[45].toFloatOrNull(),
                    visibilityControl2Y = row[46].toFloatOrNull(),
                    windSpeed = row[47].toFloatOrNull(),
                    windSpeedControl1X = row[48].toFloatOrNull(),
                    windSpeedControl1Y = row[49].toFloatOrNull(),
                    windSpeedControl2X = row[50].toFloatOrNull(),
                    windSpeedControl2Y = row[51].toFloatOrNull(),
                    windDirection = row[52].toFloatOrNull(),
                    windDirectionControl1X = row[53].toFloatOrNull(),
                    windDirectionControl1Y = row[54].toFloatOrNull(),
                    windDirectionControl2X = row[55].toFloatOrNull(),
                    windDirectionControl2Y = row[56].toFloatOrNull(),
                    uvIndex = row[57].toFloatOrNull(),
                    uvIndexControl1X = row[58].toFloatOrNull(),
                    uvIndexControl1Y = row[59].toFloatOrNull(),
                    uvIndexControl2X = row[60].toFloatOrNull(),
                    uvIndexControl2Y = row[61].toFloatOrNull(),
                    isDay = row[62].toIntOrNull(),
                    freezingLevelHeight = row[63].toFloatOrNull(),
                    freezingLevelHeightControl1X = row[64].toFloatOrNull(),
                    freezingLevelHeightControl1Y = row[65].toFloatOrNull(),
                    freezingLevelHeightControl2X = row[66].toFloatOrNull(),
                    freezingLevelHeightControl2Y = row[67].toFloatOrNull(),
                    directRadiation = row[68].toFloatOrNull(),
                    directRadiationControl1X = row[69].toFloatOrNull(),
                    directRadiationControl1Y = row[70].toFloatOrNull(),
                    directRadiationControl2X = row[71].toFloatOrNull(),
                    directRadiationControl2Y = row[72].toFloatOrNull(),
                    directNormalIrradiance = row[73].toFloatOrNull(),
                    directNormalIrradianceControl1X = row[74].toFloatOrNull(),
                    directNormalIrradianceControl1Y = row[75].toFloatOrNull(),
                    directNormalIrradianceControl2X = row[76].toFloatOrNull(),
                    directNormalIrradianceControl2Y = row[77].toFloatOrNull(),
                    snowDepth = row[78].toFloatOrNull()
                )
            } catch (e: Exception) {
                // Log error and skip malformed rows
                println("Error parsing row: $row - ${e.message}")
                null
            }
        }
    }

    private fun parseCsvToHourlyAirQuality(csvRows: List<List<String>>): List<LocalAirQuality> {
        return csvRows.mapNotNull { row ->
            try {
                // Map CSV columns to HourlyAirQuality properties
                // Adjust indices based on your CSV structure
                LocalAirQuality(
                    locationId = row[0].toInt(),
                    time = row[1],
                    pm10 = row[2].toFloatOrNull(),
                    pm10Control1X = row[3].toFloatOrNull(),
                    pm10Control1Y = row[4].toFloatOrNull(),
                    pm10Control2X = row[5].toFloatOrNull(),
                    pm10Control2Y = row[6].toFloatOrNull(),
                    pm25 = row[7].toFloatOrNull(),
                    pm25Control1X = row[8].toFloatOrNull(),
                    pm25Control1Y = row[9].toFloatOrNull(),
                    pm25Control2X = row[10].toFloatOrNull(),
                    pm25Control2Y = row[11].toFloatOrNull(),
                    carbonMonoxide = row[12].toFloatOrNull(),
                    coControl1X = row[13].toFloatOrNull(),
                    coControl1Y = row[14].toFloatOrNull(),
                    coControl2X = row[15].toFloatOrNull(),
                    coControl2Y = row[16].toFloatOrNull(),
                    nitrogenDioxide = row[17].toFloatOrNull(),
                    no2Control1X = row[18].toFloatOrNull(),
                    no2Control1Y = row[19].toFloatOrNull(),
                    no2Control2X = row[20].toFloatOrNull(),
                    no2Control2Y = row[21].toFloatOrNull(),
                    sulphurDioxide = row[22].toFloatOrNull(),
                    so2Control1X = row[23].toFloatOrNull(),
                    so2Control1Y = row[24].toFloatOrNull(),
                    so2Control2X = row[25].toFloatOrNull(),
                    so2Control2Y = row[26].toFloatOrNull(),
                    ozone = row[27].toFloatOrNull(),
                    o3Control1X = row[28].toFloatOrNull(),
                    o3Control1Y = row[29].toFloatOrNull(),
                    o3Control2X = row[30].toFloatOrNull(),
                    o3Control2Y = row[31].toFloatOrNull(),
                    airQuality = row[32].toIntOrNull(),
                    aqiControl1X = row[33].toFloatOrNull(),
                    aqiControl1Y = row[34].toFloatOrNull(),
                    aqiControl2X = row[35].toFloatOrNull(),
                    aqiControl2Y = row[36].toFloatOrNull()
                )
            } catch (e: Exception) {
                // Log error and skip malformed rows
                println("Error parsing row: $row - ${e.message}")
                null
            }
        }
    }

    private fun parseCsvToDailyWeather(csvRows: List<List<String>>): List<LocalDailyWeather> {
        return csvRows.mapNotNull { row ->
            try {
                // Map CSV columns to LocalDailyWeather properties
                // Adjust indices based on your CSV structure
                LocalDailyWeather(
                    locationId = row[0].toInt(),
                    time = row[1],
                    weatherCode = row[2].toIntOrNull(),
                    temperatureMax = row[3].toFloatOrNull(),
                    temperatureMin = row[4].toFloatOrNull(),
                    sunRise = row[5],
                    sunSet = row[6],
                    uvIndexMax = row[7].toFloatOrNull(),
                    precipitationSum = row[8].toFloatOrNull(),
                    precipitationProbabilityMax = row[9].toFloatOrNull(),
                    windSpeedMax = row[10].toFloatOrNull(),
                    windGustsMax = row[11].toFloatOrNull()
                )
            } catch (e: Exception) {
                // Log error and skip malformed rows
                println("Error parsing row: $row - ${e.message}")
                null
            }
        }
    }

    @Test
    fun addHourlyWeather_getAllHourlyWeather() = runTest {
        val csvData = loadCsvFromResources("hourly_weather.csv")
        val hourlyWeatherData = parseCsvToLocalHourlyWeather(csvData)
        locationDao.addLocation(myLocation)
        weatherDao.addHourlyWeather(hourlyWeatherData)
        val locationHourlyWeather = weatherDao.getLocationHourlyWeatherFlow(
            34,
            "2025-12-01",
            "2025-12-10"
        ).first()
        assertThat(locationHourlyWeather.size).isEqualTo(10 * 24)
    }
    
    @Test
    fun insertColumn_getLocationHistoricHourlyWeather() = runTest {
        locationDao.addLocation(myLocation)
        val temperatureHistoricData = listOf(
            SingleQuantityHourlyWeather.TemperatureHourlyWeather(
                locationId = 34,
                time = "2025-12-01T00:00",
                temperature = 5.7f,
                weatherCode = 2,
                isDay = 0
            ),
            SingleQuantityHourlyWeather.TemperatureHourlyWeather(
                locationId = 34,
                time = "2025-12-01T01:00",
                temperature = 4.9f,
                weatherCode = 3,
                isDay = 0
            ),
            SingleQuantityHourlyWeather.TemperatureHourlyWeather(
                locationId = 34,
                time = "2025-12-01T02:00",
                temperature = 4.3f,
                weatherCode = 3,
                isDay = 0
            )
        )
        weatherDao.insertColumn(temperatureHistoricData)
        var historicHourlyData = weatherDao.getLocationHistoricHourlyWeather(
            34,
            "2025-12-01",
            "2025-12-01"
        )
        assertThat(historicHourlyData.size).isEqualTo(3)
        assertThat(historicHourlyData.first().temperature).isEqualTo(5.7f)
        assertThat(historicHourlyData[1].apparentTemperature).isNull()
        weatherDao.insertColumn(
            listOf(
                SingleQuantityHourlyWeather.ApparentTemperatureHourlyWeather(
                    locationId = 34,
                    time = "2025-12-01T02:00",
                    apparentTemperature = 2.5f,
                    weatherCode = 1,
                    isDay = 0
                )
            )
        )
        historicHourlyData = weatherDao.getLocationHistoricHourlyWeather(
            34,
            "2025-12-01",
            "2025-12-01"
        )
        assertThat(historicHourlyData[2].apparentTemperature).isEqualTo(2.5f)
        assertThat(historicHourlyData[2].weatherCode).isEqualTo(1)
    }
    @Test
    fun historicHourlyWeather_trimTable_test() = runTest {
        locationDao.addLocation(myLocation)
        val temperatureHistoricData = listOf(
            SingleQuantityHourlyWeather.TemperatureHourlyWeather(
                locationId = 34,
                time = "2025-12-01T00:00",
                temperature = 5.7f,
                weatherCode = 2,
                isDay = 0,
                timeStamp = 1000
            ),
            SingleQuantityHourlyWeather.TemperatureHourlyWeather(
                locationId = 34,
                time = "2025-12-01T01:00",
                temperature = 4.9f,
                weatherCode = 3,
                isDay = 0,
                timeStamp = 1001
            ),
            SingleQuantityHourlyWeather.TemperatureHourlyWeather(
                locationId = 34,
                time = "2025-12-01T02:00",
                temperature = 4.3f,
                weatherCode = 3,
                isDay = 0,
                timeStamp = 1002
            )
        )
        weatherDao.insertColumn(temperatureHistoricData)
        weatherDao.trimTable(2)
        val historicHourlyData = weatherDao.getLocationHistoricHourlyWeather(
            34,
            "2025-12-01",
            "2025-12-01"
        )
        assertThat(historicHourlyData.size).isEqualTo(2)
        assertThat(historicHourlyData[0].time).isEqualTo("2025-12-01T01:00")
    }

}