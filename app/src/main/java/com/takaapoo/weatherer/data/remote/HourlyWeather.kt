package com.takaapoo.weatherer.data.remote

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.util.fastForEachIndexed
import com.takaapoo.weatherer.data.local.LocalHourlyWeather
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.indicesOf
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.nonNullDataSegments
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

const val NumberOfDays = 18
const val DayHoursCount = 24
val HourlyWeatherDefaultValues = List(NumberOfDays * DayHoursCount){ null }
val HourlyWeatherDefaultTimes = List(17){ dayIndex ->
    val localDate = LocalDate.now(ZoneId.of("UTC"))
        .minusDays(1).plusDays(dayIndex.toLong())
    List(24){ hourIndex ->
        val localTime = LocalTime.MIDNIGHT
        "${localDate}T${localTime.plusHours(hourIndex.toLong())}"
    }
}.flatten()

@Serializable
data class HourlyWeather(
    val time: List<String> = HourlyWeatherDefaultTimes,
    @SerialName(value = "temperature_2m") val temperature: List<Float?> = HourlyWeatherDefaultValues,
    @SerialName(value = "relativehumidity_2m") val humidity: List<Float?> = HourlyWeatherDefaultValues,
    @SerialName(value = "dewpoint_2m") val dewPoint: List<Float?> = HourlyWeatherDefaultValues,
    @SerialName(value = "apparent_temperature") val apparentTemperature: List<Float?> = HourlyWeatherDefaultValues,
    @SerialName(value = "precipitation_probability") val precipitationProbability: List<Float?> = HourlyWeatherDefaultValues,
    @SerialName(value = "precipitation") val precipitation: List<Float?> = HourlyWeatherDefaultValues,
    @SerialName(value = "rain") val rain: List<Float?> = HourlyWeatherDefaultValues,
    @SerialName(value = "showers") val showers: List<Float?> = HourlyWeatherDefaultValues,
    @SerialName(value = "snowfall") val snowfall: List<Float?> = HourlyWeatherDefaultValues,
    @SerialName(value = "cloud_cover") val cloudCover: List<Float?> = HourlyWeatherDefaultValues,
    @SerialName(value = "weathercode") val weatherCode: List<Int?> = HourlyWeatherDefaultValues,
    @SerialName(value = "surface_pressure") val surfacePressure: List<Float?> = HourlyWeatherDefaultValues,
    @SerialName(value = "visibility") val visibility: List<Float?> = HourlyWeatherDefaultValues,
    @SerialName(value = "windspeed_10m") val windSpeed: List<Float?> = HourlyWeatherDefaultValues,
    @SerialName(value = "winddirection_10m") val windDirection: List<Float?> = HourlyWeatherDefaultValues,
    @SerialName(value = "uv_index") val uvIndex: List<Float?> = HourlyWeatherDefaultValues,
    @SerialName(value = "is_day") val isDay: List<Int?> = HourlyWeatherDefaultValues,
    @SerialName(value = "freezinglevel_height") val freezingLevelHeight: List<Float?> = HourlyWeatherDefaultValues,
    @SerialName(value = "direct_radiation") val directRadiation: List<Float?> = HourlyWeatherDefaultValues,
    @SerialName(value = "direct_normal_irradiance") val directNormalIrradiance: List<Float?> = HourlyWeatherDefaultValues
)

fun HourlyWeather.toLocalHourlyWeather(
    locationId: Int,
    lastLocalHourlyWeather: LocalHourlyWeather? = null
): List<LocalHourlyWeather> {
    val extendedData = if (lastLocalHourlyWeather != null){
        HourlyWeather(
            time = time.toMutableList().apply { add(index = 0, element = lastLocalHourlyWeather.time) },
            temperature = temperature.toMutableList().apply { add(index = 0, element = lastLocalHourlyWeather.temperature) },
            humidity = humidity.toMutableList().apply { add(index = 0, element = lastLocalHourlyWeather.humidity) },
            dewPoint = dewPoint.toMutableList().apply { add(index = 0, element = lastLocalHourlyWeather.dewPoint) },
            apparentTemperature = apparentTemperature.toMutableList().apply {
                add(index = 0, element = lastLocalHourlyWeather.apparentTemperature) },
            precipitationProbability = precipitationProbability.toMutableList().apply {
                add(index = 0, element = lastLocalHourlyWeather.precipitationProbability) },
            precipitation = precipitation.toMutableList().apply {
                add(index = 0, element = lastLocalHourlyWeather.precipitation) },
            rain = rain.toMutableList().apply { add(index = 0, element = lastLocalHourlyWeather.rain) },
            showers = showers.toMutableList().apply { add(index = 0, element = lastLocalHourlyWeather.showers) },
            snowfall = snowfall.toMutableList().apply { add(index = 0, element = lastLocalHourlyWeather.snowfall) },
            cloudCover = cloudCover.toMutableList().apply {
                add(index = 0, element = lastLocalHourlyWeather.cloudCover) },
            weatherCode = weatherCode.toMutableList().apply { add(index = 0, element = lastLocalHourlyWeather.weatherCode) },
            surfacePressure = surfacePressure.toMutableList().apply { add(index = 0, element = lastLocalHourlyWeather.surfacePressure) },
            visibility = visibility.map { it?.div(1000f) }.toMutableList()
                .apply { add(index = 0, element = lastLocalHourlyWeather.visibility) },
            windSpeed = windSpeed.toMutableList().apply { add(index = 0, element = lastLocalHourlyWeather.windSpeed) },
            windDirection = windDirection.toMutableList().apply { add(index = 0, element = lastLocalHourlyWeather.windDirection) },
            uvIndex = uvIndex.toMutableList().apply { add(index = 0, element = lastLocalHourlyWeather.uvIndex) },
            isDay = isDay.toMutableList().apply { add(index = 0, element = lastLocalHourlyWeather.isDay) },
            freezingLevelHeight = freezingLevelHeight.toMutableList().apply {
                add(index = 0, element = lastLocalHourlyWeather.freezingLevelHeight) },
            directRadiation = directRadiation.toMutableList().apply {
                add(index = 0, element = lastLocalHourlyWeather.directRadiation) },
            directNormalIrradiance = directNormalIrradiance.toMutableList().apply {
                add(index = 0, element = lastLocalHourlyWeather.directNormalIrradiance) }
            )
    } else this.copy(visibility = visibility.map { it?.div(1000f) })

    val (temperatureControl1, temperatureControl2) =
        generateControlPoints2(extendedData.temperature)
    val (humidityControl1, humidityControl2) =
        generateControlPoints2(extendedData.humidity)
    val (dewPointControl1, dewPointControl2) =
        generateControlPoints2(extendedData.dewPoint)
    val (apparentTemperatureControl1, apparentTemperatureControl2) =
        generateControlPoints2(extendedData.apparentTemperature)
    val (precipitationProbabilityControl1, precipitationProbabilityControl2) =
        generateControlPoints2(extendedData.precipitationProbability)
    val (cloudCoverControl1, cloudCoverControl2) =
        generateControlPoints2(extendedData.cloudCover)
    val (surfacePressureControl1, surfacePressureControl2) =
        generateControlPoints2(extendedData.surfacePressure)
    val (visibilityControl1, visibilityControl2) =
        generateControlPoints2(extendedData.visibility)
    val (windSpeedControl1, windSpeedControl2) =
        generateControlPoints2(extendedData.windSpeed)
    val (windDirectionControl1, windDirectionControl2) =
        generateControlPoints2(extendedData.windDirection)
    val (uvIndexControl1, uvIndexControl2) =
        generateControlPoints2(extendedData.uvIndex)
    val (freezingLevelHeightControl1, freezingLevelHeightControl2) =
        generateControlPoints2(extendedData.freezingLevelHeight)
    val (directRadiationControl1, directRadiationControl2) =
        generateControlPoints2(extendedData.directRadiation)
    val (directNormalIrradianceControl1, directNormalIrradianceControl2) =
        generateControlPoints2(extendedData.directNormalIrradiance)



    return List(size = time.size){ i ->
        LocalHourlyWeather(
            locationId = locationId,
            time = extendedData.time[i],
            temperature = extendedData.temperature[i],
            temperatureControl1X = temperatureControl1.getOrNull(i)?.x,
            temperatureControl1Y = temperatureControl1.getOrNull(i)?.y,
            temperatureControl2X = temperatureControl2.getOrNull(i)?.x,
            temperatureControl2Y = temperatureControl2.getOrNull(i)?.y,

            humidity = extendedData.humidity[i],
            humidityControl1X = humidityControl1.getOrNull(i)?.x,
            humidityControl1Y = humidityControl1.getOrNull(i)?.y,
            humidityControl2X = humidityControl2.getOrNull(i)?.x,
            humidityControl2Y = humidityControl2.getOrNull(i)?.y,

            dewPoint = extendedData.dewPoint[i],
            dewPointControl1X = dewPointControl1.getOrNull(i)?.x,
            dewPointControl1Y = dewPointControl1.getOrNull(i)?.y,
            dewPointControl2X = dewPointControl2.getOrNull(i)?.x,
            dewPointControl2Y = dewPointControl2.getOrNull(i)?.y,

            apparentTemperature = extendedData.apparentTemperature[i],
            apparentTemperatureControl1X = apparentTemperatureControl1.getOrNull(i)?.x,
            apparentTemperatureControl1Y = apparentTemperatureControl1.getOrNull(i)?.y,
            apparentTemperatureControl2X = apparentTemperatureControl2.getOrNull(i)?.x,
            apparentTemperatureControl2Y = apparentTemperatureControl2.getOrNull(i)?.y,


            precipitationProbability = extendedData.precipitationProbability[i],
            precipitationProbabilityControl1X = precipitationProbabilityControl1.getOrNull(i)?.x,
            precipitationProbabilityControl1Y = precipitationProbabilityControl1.getOrNull(i)?.y,
            precipitationProbabilityControl2X = precipitationProbabilityControl2.getOrNull(i)?.x,
            precipitationProbabilityControl2Y = precipitationProbabilityControl2.getOrNull(i)?.y,

            precipitation = extendedData.precipitation[i],
            rain = extendedData.rain[i],
            showers = extendedData.showers[i],
            snowfall = extendedData.snowfall[i],
            weatherCode = extendedData.weatherCode[i],

            cloudCover = extendedData.cloudCover[i],
            cloudCoverControl1X = cloudCoverControl1.getOrNull(i)?.x,
            cloudCoverControl1Y = cloudCoverControl1.getOrNull(i)?.y,
            cloudCoverControl2X = cloudCoverControl2.getOrNull(i)?.x,
            cloudCoverControl2Y = cloudCoverControl2.getOrNull(i)?.y,

            surfacePressure = extendedData.surfacePressure[i],
            surfacePressureControl1X = surfacePressureControl1.getOrNull(i)?.x,
            surfacePressureControl1Y = surfacePressureControl1.getOrNull(i)?.y,
            surfacePressureControl2X = surfacePressureControl2.getOrNull(i)?.x,
            surfacePressureControl2Y = surfacePressureControl2.getOrNull(i)?.y,

            visibility = extendedData.visibility[i],
            visibilityControl1X = visibilityControl1.getOrNull(i)?.x,
            visibilityControl1Y = visibilityControl1.getOrNull(i)?.y,
            visibilityControl2X = visibilityControl2.getOrNull(i)?.x,
            visibilityControl2Y = visibilityControl2.getOrNull(i)?.y,

            windSpeed = extendedData.windSpeed[i],
            windSpeedControl1X = windSpeedControl1.getOrNull(i)?.x,
            windSpeedControl1Y = windSpeedControl1.getOrNull(i)?.y,
            windSpeedControl2X = windSpeedControl2.getOrNull(i)?.x,
            windSpeedControl2Y = windSpeedControl2.getOrNull(i)?.y,

            windDirection = extendedData.windDirection[i],
            windDirectionControl1X = windDirectionControl1.getOrNull(i)?.x,
            windDirectionControl1Y = windDirectionControl1.getOrNull(i)?.y,
            windDirectionControl2X = windDirectionControl2.getOrNull(i)?.x,
            windDirectionControl2Y = windDirectionControl2.getOrNull(i)?.y,

            uvIndex = extendedData.uvIndex[i],
            uvIndexControl1X = uvIndexControl1.getOrNull(i)?.x,
            uvIndexControl1Y = uvIndexControl1.getOrNull(i)?.y,
            uvIndexControl2X = uvIndexControl2.getOrNull(i)?.x,
            uvIndexControl2Y = uvIndexControl2.getOrNull(i)?.y,

            isDay = extendedData.isDay[i],
            freezingLevelHeight = extendedData.freezingLevelHeight[i],
            freezingLevelHeightControl1X = freezingLevelHeightControl1.getOrNull(i)?.x,
            freezingLevelHeightControl1Y = freezingLevelHeightControl1.getOrNull(i)?.y,
            freezingLevelHeightControl2X = freezingLevelHeightControl2.getOrNull(i)?.x,
            freezingLevelHeightControl2Y = freezingLevelHeightControl2.getOrNull(i)?.y,

            directRadiation = extendedData.directRadiation[i],
            directRadiationControl1X = directRadiationControl1.getOrNull(i)?.x,
            directRadiationControl1Y = directRadiationControl1.getOrNull(i)?.y,
            directRadiationControl2X = directRadiationControl2.getOrNull(i)?.x,
            directRadiationControl2Y = directRadiationControl2.getOrNull(i)?.y,

            directNormalIrradiance = extendedData.directNormalIrradiance[i],
            directNormalIrradianceControl1X = directNormalIrradianceControl1.getOrNull(i)?.x,
            directNormalIrradianceControl1Y = directNormalIrradianceControl1.getOrNull(i)?.y,
            directNormalIrradianceControl2X = directNormalIrradianceControl2.getOrNull(i)?.x,
            directNormalIrradianceControl2Y = directNormalIrradianceControl2.getOrNull(i)?.y
        )
    }
}

fun generateControlPoints2(data: List<Float?>): Pair<List<Offset?>, List<Offset?>> {
    if (data.size < 2) return listOf(null) to listOf(null)

    val p: MutableList<Offset> = mutableListOf()
    data.fastForEachIndexed { index, value ->
        value?.let {
            p.add( Offset(index.toFloat(), it) )
        }
    }

    val n = p.size - 1
    if (p.size == 2) return listOf((p[0] * 2f + p[1]) / 3f) to listOf((p[0] + p[1] * 2f)/3f)

    val a = List(size = n){
        when (it) {
            0 -> 0
            n-1 -> 2
            else -> 1
        }
    }
    val b = List(size = n){
        when (it) {
            0 -> 2
            n-1 -> 7
            else -> 4
        }
    }
    val c = List(size = n){
        when (it) {
            n-1 -> 0
            else -> 1
        }
    }
    val d = List(size = n){
        when (it){
            0 -> p[0] + p[1] * 2f
            n-1 -> p[n-1] * 8f + p[n]
            else -> (p[it]*2f + p[it+1]) * 2f
        }
    }
    val cPrime = MutableList(size = n-1){ 0f }
    cPrime.forEachIndexed { index, _ ->
        when (index){
            0 -> cPrime[0] = c[0].toFloat() / b[0]
            else -> cPrime[index] = c[index] / (b[index] - a[index]*cPrime[index-1])
        }
    }
    val dPrime = MutableList(size = n){ Offset(0f, 0f) }
    dPrime.forEachIndexed { index, _ ->
        when (index){
            0 -> dPrime[0] = d[0] / b[0].toFloat()
            else -> dPrime[index] = (d[index] - dPrime[index-1] * a[index].toFloat()) /
                    (b[index] - a[index]*cPrime[index-1])
        }
    }

    val dataNullIndices = data.indicesOf(null)

    var controlPoint1: MutableList<Offset?> = MutableList(size = n){ Offset(0f, 0f) }
    for (index in n-1 downTo 0){
        controlPoint1[index] = when (index) {
            n - 1 -> dPrime[n - 1]
            else -> dPrime[index] - controlPoint1[index + 1]!! * cPrime[index]
        }
    }
    var controlPoint2: MutableList<Offset?> = MutableList(size = n){ Offset(0f, 0f) }
    for (index in n-1 downTo 0){
        controlPoint2[index] = when (index){
            n-1 -> (controlPoint1[n-1]!! + p[n]) / 2f
            else -> p[index+1] * 2f - controlPoint1[index+1]!!
        }
    }
    controlPoint1 = controlPoint1.mapIndexed { index, offset ->
        offset!! - Offset(x = p[index].x, y = 0f)
    }.toMutableList()
    controlPoint2 = controlPoint2.mapIndexed { index, offset ->
        offset!! - Offset(x = p[index].x, y = 0f)
    }.toMutableList()

    dataNullIndices.forEach {
        if (it >= controlPoint1.size){
            controlPoint1.add(null)
            controlPoint2.add(null)
        } else {
            controlPoint1.add(index = it, null)
            controlPoint2.add(index = it, null)
        }
    }
    return controlPoint1 to controlPoint2
}

fun generateControlPoints(data: List<Float>): Pair<List<Offset?>, List<Offset?>> {
//    val m = data.indexOf(null)
    val n = data.size-1 /*if (m == -1) data.size-1 else m-1*/
    if (n < 1) return listOf(null) to listOf(null)

    val p = List(size = n+1){ index ->
        Offset(index.toFloat(), data[index])
    }
    if (n == 1) return listOf((p[0] * 2f + p[1]) / 3f) to listOf((p[0] + p[1] * 2f)/3f)

    val a = List(size = n){
        when (it) {
            0 -> 0
            n-1 -> 2
            else -> 1
        }
    }
    val b = List(size = n){
        when (it) {
            0 -> 2
            n-1 -> 7
            else -> 4
        }
    }
    val c = List(size = n){
        when (it) {
            n-1 -> 0
            else -> 1
        }
    }
    val d = List(size = n){
        when (it){
            0 -> p[0] + p[1] * 2f
            n-1 -> p[n-1] * 8f + p[n]
            else -> (p[it]*2f + p[it+1]) * 2f
        }
    }
    val cPrime = MutableList(size = n-1){ 0f }
    cPrime.forEachIndexed { index, _ ->
        when (index){
            0 -> cPrime[0] = c[0].toFloat() / b[0]
            else -> cPrime[index] = c[index] / (b[index] - a[index]*cPrime[index-1])
        }
    }
    val dPrime = MutableList(size = n){ Offset(0f, 0f) }
    dPrime.forEachIndexed { index, _ ->
        when (index){
            0 -> dPrime[0] = d[0] / b[0].toFloat()
            else -> dPrime[index] = (d[index] - dPrime[index-1] * a[index].toFloat()) /
                    (b[index] - a[index]*cPrime[index-1])
        }
    }

    val controlPoint1 = MutableList(size = n){ Offset(0f, 0f) }
    for (index in n-1 downTo 0){
        controlPoint1[index] = when (index){
            n-1 -> dPrime[n-1]
            else -> dPrime[index] - controlPoint1[index+1]*cPrime[index]
        }
    }
    val controlPoint2 = MutableList(size = n){ Offset(0f, 0f) }
    for (index in n-1 downTo 0){
        controlPoint2[index] = when (index){
            n-1 -> (controlPoint1[n-1] + p[n]) / 2f
            else -> p[index+1] * 2f - controlPoint1[index+1]
        }
    }
    return controlPoint1.mapIndexed { index, offset -> offset - Offset(x = index.toFloat(), y = 0f) } to
            controlPoint2.mapIndexed { index, offset -> offset - Offset(x = index.toFloat(), y = 0f) }
}

fun generateControlPointsForNullableData(data: List<Float?>): Pair<List<Offset?>, List<Offset?>> {
    val dataList = data.nonNullDataSegments()
    val n = data.size - 1
    val controlPoint1: MutableList<Offset?> = MutableList(size = n){ null }
    val controlPoint2: MutableList<Offset?> = MutableList(size = n){ null }

    dataList.forEach{
        val (cntr1, cntr2) = generateControlPoints(it.value)
        val subList1 = controlPoint1.subList(it.key, it.key + cntr1.size)
        subList1.clear()
        subList1.addAll(cntr1)
//        controlPoint1.addAll(index = it.key, elements = cntr1)
//        controlPoint2.addAll(index = it.key, elements = cntr2)
        val subList2 = controlPoint2.subList(it.key, it.key + cntr2.size)
        subList2.clear()
        subList2.addAll(cntr2)
    }
    return controlPoint1 to controlPoint2
}


private fun generateControlPointsWithInitialCondition(
    data: List<Float?>,
    initialGradient: Offset,
    initialCurvature: Offset
): Pair<List<Offset>?, List<Offset>?> {
    val m = data.indexOf(null)
    val n = if (m == -1) data.size-1 else m-1
    if (n < 1) return null to null

    val p = List(size = n+1){ index ->
        Offset(index.toFloat(), data[index]!!)
    }

    if (n == 1) return listOf(p[0] + initialGradient / 3f) to
            listOf(initialCurvature/6f + p[0] + initialGradient * (2f/3f))

    val controlPoint1 = MutableList(size = n){ Offset(0f, 0f) }
    for (index in 0 until n){
        controlPoint1[index] = when (index){
            0 -> p[0] + initialGradient / 3f
            1 -> p[1] * 2f - (initialCurvature/6f + p[0] + initialGradient * (2f/3f))
            else -> (p[index-1]*2f + p[index]) * 2f - controlPoint1[index-1]*4f - controlPoint1[index -2]
        }
    }
    val controlPoint2 = MutableList(size = n){ Offset(0f, 0f) }
    for (index in 0 until n){
        controlPoint2[index] = when (index){
            0 -> initialCurvature/6f + p[0] + initialGradient * (2f/3f)
            n-1 -> controlPoint1[n-2] + controlPoint1[n-1]*2f - controlPoint2[n-2]*2f
            else -> p[index+1] * 2f - controlPoint1[index+1]
        }
    }

    return controlPoint1.mapIndexed { index, offset -> offset - Offset(x = index.toFloat(), y = 0f) } to
            controlPoint2.mapIndexed { index, offset -> offset - Offset(x = index.toFloat(), y = 0f) }
}