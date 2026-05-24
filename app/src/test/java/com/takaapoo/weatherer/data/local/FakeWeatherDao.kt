package com.takaapoo.weatherer.data.local

import com.takaapoo.weatherer.utility.dateIsBetween
import com.takaapoo.weatherer.utility.timeIsBetween
import com.takaapoo.weatherer.utility.toFloatOrNull
import com.takaapoo.weatherer.utility.toIntOrNull
import kotlinx.coroutines.flow.Flow
import java.io.InputStream


fun parseCsvToLocalHourlyWeather(csvRows: List<List<String>>): List<LocalHourlyWeather> {
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

fun parseCsvToHourlyAirQuality(csvRows: List<List<String>>): List<LocalAirQuality> {
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

fun parseCsvToDailyWeather(csvRows: List<List<String>>): List<LocalDailyWeather> {
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

class FakeWeatherDao: WeatherDao {
    private fun loadCsvFromResources(fileName: String): List<List<String>> {
        // Load file from test/resources
        val inputStream: InputStream = javaClass.classLoader
            ?.getResourceAsStream(fileName)
            ?: throw IllegalArgumentException("File not found: $fileName")

        // Parse CSV (using manual parser or library)
        return CsvParser.parseCsv(inputStream, hasHeader = true)
    }

    override suspend fun addHourlyWeather(hourlyWeathers: List<LocalHourlyWeather>) {
        TODO("Not yet implemented")
    }

    override suspend fun addDailyWeather(dailyWeathers: List<LocalDailyWeather>) {
        TODO("Not yet implemented")
    }

    override suspend fun addAirQuality(airQuality: List<LocalAirQuality>) {
        TODO("Not yet implemented")
    }

    override fun getAllHourlyWeather(): Flow<Map<Location, List<LocalHourlyWeather>>> {
        TODO("Not yet implemented")
    }

    override fun getLocationHourlyWeatherFlow(
        locationId: Int,
        startDate: String,
        endDate: String,
    ): Flow<List<LocalHourlyWeather>> {
        TODO("Not yet implemented")
    }

    override suspend fun getLocationHourlyWeather(
        locationId: Int,
        startDate: String,
        endDate: String,
    ): List<LocalHourlyWeather> {
        TODO("Not yet implemented")
    }

    override suspend fun getHourlyWeather(
        startDate: String,
        endDate: String,
    ): Map<Location, List<LocalHourlyWeather>> {
        val csvData = loadCsvFromResources("hourly_weather.csv")
        val hourlyWeatherData = parseCsvToLocalHourlyWeather(csvData).filter {
            it.time.timeIsBetween(startDate, endDate)
        }
        return mapOf(testLocation to hourlyWeatherData, testLocation2 to hourlyWeatherData)
    }

    override suspend fun getHourlyAirQuality(
        startDate: String,
        endDate: String,
    ): Map<Location, List<LocalAirQuality>> {
        val csvData = loadCsvFromResources("air_quality.csv")
        val airQualityData = parseCsvToHourlyAirQuality(csvData).filter {
            it.time.timeIsBetween(startDate, endDate)
        }
        return mapOf(testLocation to airQualityData, testLocation2 to airQualityData)
    }

    override suspend fun getLocationHourlyWeatherForTime(
        locationId: Int,
        startTime: String,
        endTime: String,
    ): List<LocalHourlyWeather> {
        TODO("Not yet implemented")
    }

    override suspend fun getDailyWeather(
        startDate: String,
        endDate: String,
    ): Map<Location, List<LocalDailyWeather>> {
        val csvData = loadCsvFromResources("daily_weather.csv")
        val dailyWeatherData = parseCsvToDailyWeather(csvData).filter {
            it.time.dateIsBetween(startDate, endDate)
        }
        return mapOf(testLocation to dailyWeatherData, testLocation2 to dailyWeatherData)
    }

    override fun getLocationDailyWeatherFlow(
        locationId: Int,
        startDate: String,
        endDate: String,
    ): Flow<List<LocalDailyWeather>> {
        TODO("Not yet implemented")
    }

    override suspend fun getLocationDailyWeather(
        locationId: Int,
        startDate: String,
        endDate: String,
    ): List<LocalDailyWeather> {
        TODO("Not yet implemented")
    }

    override fun getAllAirQuality(): Flow<Map<Location, List<LocalAirQuality>>> {
        TODO("Not yet implemented")
    }

    override fun getLocationAirQualityFlow(
        locationId: Int,
        startDate: String,
        endDate: String,
    ): Flow<List<LocalAirQuality>> {
        TODO("Not yet implemented")
    }

    override suspend fun getLocationAirQuality(
        locationId: Int,
        startDate: String,
        endDate: String,
    ): List<LocalAirQuality> {
        TODO("Not yet implemented")
    }

    override fun getAllTimes(): Flow<List<String>> {
        TODO("Not yet implemented")
    }

    override suspend fun getLocationHistoricHourlyWeather(
        locationId: Int,
        startDate: String,
        endDate: String,
    ): List<LocalHistoricHourlyWeather> {
        TODO("Not yet implemented")
    }

    override suspend fun getRowCounts(): Long {
        TODO("Not yet implemented")
    }

    override suspend fun trimTable(maxRows: Int) {
        TODO("Not yet implemented")
    }

    override fun insertTemperature(temperatures: List<SingleQuantityHourlyWeather.TemperatureHourlyWeather>) {
        TODO("Not yet implemented")
    }

    override fun insertHumidity(humidities: List<SingleQuantityHourlyWeather.HumidityHourlyWeather>) {
        TODO("Not yet implemented")
    }

    override fun insertDewPoint(dewPoints: List<SingleQuantityHourlyWeather.DewPointHourlyWeather>) {
        TODO("Not yet implemented")
    }

    override fun insertApparentTemperature(temperatures: List<SingleQuantityHourlyWeather.ApparentTemperatureHourlyWeather>) {
        TODO("Not yet implemented")
    }

    override fun insertPrecipitation(precipitations: List<SingleQuantityHourlyWeather.PrecipitationHourlyWeather>) {
        TODO("Not yet implemented")
    }

    override fun insertRain(rains: List<SingleQuantityHourlyWeather.RainHourlyWeather>) {
        TODO("Not yet implemented")
    }

    override fun insertSnowfall(snowfalls: List<SingleQuantityHourlyWeather.SnowfallHourlyWeather>) {
        TODO("Not yet implemented")
    }

    override fun insertWeatherCode(codes: List<SingleQuantityHourlyWeather.WeatherCodeHourlyWeather>) {
        TODO("Not yet implemented")
    }

    override fun insertCloudCover(cloudCovers: List<SingleQuantityHourlyWeather.CloudCoverHourlyWeather>) {
        TODO("Not yet implemented")
    }

    override fun insertSurfacePressure(pressures: List<SingleQuantityHourlyWeather.SurfacePressureHourlyWeather>) {
        TODO("Not yet implemented")
    }

    override fun insertWindSpeed(speeds: List<SingleQuantityHourlyWeather.WindSpeedHourlyWeather>) {
        TODO("Not yet implemented")
    }

    override fun insertWindDirection(directions: List<SingleQuantityHourlyWeather.WindDirectionHourlyWeather>) {
        TODO("Not yet implemented")
    }

    override fun insertIsDay(isDays: List<SingleQuantityHourlyWeather.IsDayHourlyWeather>) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteHistoricHourlyWeather() {
        TODO("Not yet implemented")
    }

    override suspend fun getLocationHistoricDailyWeather(
        locationId: Int,
        startDate: String,
        endDate: String,
    ): List<LocalHistoricDailyWeather> {
        TODO("Not yet implemented")
    }

    override suspend fun trimDailyTable(maxRows: Int) {
        TODO("Not yet implemented")
    }

    override fun insertDailyTemperature(temperatures: List<SingleQuantityDailyWeather.TemperatureMinMaxDailyWeather>) {
        TODO("Not yet implemented")
    }

    override fun insertDailyTemperatureMean(temperatures: List<SingleQuantityDailyWeather.TemperatureMeanDailyWeather>) {
        TODO("Not yet implemented")
    }

    override fun insertDailyCloudCover(temperatures: List<SingleQuantityDailyWeather.CloudCoverDailyWeather>) {
        TODO("Not yet implemented")
    }

    override fun insertDailyHumidity(temperatures: List<SingleQuantityDailyWeather.HumidityDailyWeather>) {
        TODO("Not yet implemented")
    }

    override fun insertDailyPrecipitation(temperatures: List<SingleQuantityDailyWeather.PrecipitationDailyWeather>) {
        TODO("Not yet implemented")
    }

    override fun insertDailyWindSpeed(temperatures: List<SingleQuantityDailyWeather.WindSpeedDailyWeather>) {
        TODO("Not yet implemented")
    }

    override fun insertDailyWindGusts(temperatures: List<SingleQuantityDailyWeather.WindGustsDailyWeather>) {
        TODO("Not yet implemented")
    }
}