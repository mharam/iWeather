package com.takaapoo.weatherer.ui.widget

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.data.local.LocalDailyWeather
import com.takaapoo.weatherer.domain.WeatherType
import com.takaapoo.weatherer.domain.WeatherType.Companion.calculateMoonType
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import com.takaapoo.weatherer.domain.model.HourlyDiagramTime
import com.takaapoo.weatherer.domain.repository.LocalWeatherRepository
import com.takaapoo.weatherer.domain.repository.PreferenceRepository
import com.takaapoo.weatherer.domain.use_case.GetHourlyChartDataUseCase
import com.takaapoo.weatherer.moonPhase
import com.takaapoo.weatherer.ui.notification.custom_views.timeToX
import com.takaapoo.weatherer.ui.screens.detail.DailyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.HistoricalState
import com.takaapoo.weatherer.ui.screens.detail.HourlyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.weatherCurveXtoY
import com.takaapoo.weatherer.ui.theme.BarBlue
import com.takaapoo.weatherer.ui.theme.BarBlueLight
import com.takaapoo.weatherer.ui.theme.Gray20
import com.takaapoo.weatherer.ui.theme.Gray40
import com.takaapoo.weatherer.utility.getColorScheme
import com.takaapoo.weatherer.utility.readMoonPhaseBinFromAssets
import com.takaapoo.weatherer.utility.renderViewToBitmap
import com.takaapoo.weatherer.utility.toPx
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToInt


class WeatherWidget2(): GlanceAppWidget() {

    private val weatherKey = intPreferencesKey("weather")
    companion object {
        val layoutWidth = intPreferencesKey("layout_width")
        val layoutHeight = intPreferencesKey("layout_height")
        val SMALL_SQUARE = DpSize(190.dp, 220.dp)
        private val MEDIUM_SQUARE = DpSize(260.dp, 340.dp)
        val BIG_SQUARE = DpSize(320.dp, 440.dp)
        private val HORIZONTAL_RECTANGLE = DpSize(260.dp, 220.dp)
        private val WIDE_RECTANGLE = DpSize(320.dp, 220.dp)
        private val BIG_HORIZONTAL_RECTANGLE = DpSize(320.dp, 340.dp)
        private val VERTICAL_RECTANGLE = DpSize(190.dp, 340.dp)
        private val TALL_RECTANGLE = DpSize(190.dp, 440.dp)
        private val BIG_VERTICAL_RECTANGLE = DpSize(260.dp, 440.dp)
    }

    override val sizeMode: SizeMode = SizeMode.Exact /*SizeMode.Responsive(
        setOf(SMALL_SQUARE, MEDIUM_SQUARE, BIG_SQUARE, HORIZONTAL_RECTANGLE, WIDE_RECTANGLE,
            BIG_HORIZONTAL_RECTANGLE, VERTICAL_RECTANGLE, TALL_RECTANGLE, BIG_VERTICAL_RECTANGLE)
    )*/
    override val stateDefinition = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val preferenceRepository = getHiltDependency<PreferenceRepository>(context)
        val localWeatherRepository = getHiltDependency<LocalWeatherRepository>(context)
        val hourlyChartDataUseCase = getHiltDependency<GetHourlyChartDataUseCase>(context)
        val appSettings = preferenceRepository.getSettings()
        val hourlyChartData = appSettings.widgetLocationId?.let {
            hourlyChartDataUseCase(
                locationId = it,
                startDate = LocalDate.now(ZoneId.of("UTC")).minusDays(1).toString(),
                endDate = LocalDate.now(ZoneId.of("UTC")).plusDays(3).toString(),
                appSettings = appSettings
            )
        } ?: emptyList()
        val dailyChartData = appSettings.widgetLocationId?.let {
            localWeatherRepository.getLocationDailyWeather(
                locationId = it,
                startDate = LocalDate.now().minusDays(2).toString(),
                endDate = LocalDate.now().plusDays(8).toString(),
                appSettings = appSettings
            )
        } ?: emptyList()

//        moonPhaseMap = Json.parseToJsonElement(readJSONFromAssets(context, "moon_phase.json"))
//            .jsonObject.toMap()
        moonPhase = readMoonPhaseBinFromAssets(context, "moon_phase.bin")

        provideContent {
            MyContent(
                context = context,
                id = id,
                hourlyChartData = hourlyChartData,
                dailyChartData = dailyChartData,
                appSettings = appSettings,
//                hourlyChartQuantities = appSettings.notificationHourlyQuantities,
            )
        }
    }

    private inline fun <reified T> getHiltDependency(context: Context): T {
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            AppWidgetEntryPoint::class.java
        )
        return when (T::class) {
            PreferenceRepository::class -> hiltEntryPoint.getPreferenceRepository() as T
            LocalWeatherRepository::class -> hiltEntryPoint.getLocalWeatherRepository() as T
            GetHourlyChartDataUseCase::class -> hiltEntryPoint.getHourlyChartDataUseCase() as T
            else -> throw IllegalArgumentException("Unknown dependency type")
        }
    }

    @Composable
    private fun MyContent(
        context: Context,
        id: GlanceId,
        hourlyChartData: List<HourlyChartDto>,
        dailyChartData: List<LocalDailyWeather>,
        appSettings: AppSettings,
//        hourlyChartQuantities: List<WeatherQuantity>,
    ) {
        val density = Density(context)
//        val size = LocalSize.current
        val layoutWidth = currentState(layoutWidth) ?: BIG_SQUARE.width.value.toInt()
        val layoutHeight = currentState(layoutHeight) ?: TALL_RECTANGLE.height.value.toInt()

        val colorScheme = getColorScheme(
            context = context,
            dynamicColor = appSettings.dynamicColor
        )
//        val dateTime = HourlyDiagramTime.LOCATION

        val firstPointX = timeToX(
            time = hourlyChartData[0].hourlyWeather!!.time,
            utcOffset = hourlyChartData[0].utcOffset ?: 0L,
            timeClock = appSettings.widgetDateTime
        )!!
        val lastPointX = timeToX(
            time = hourlyChartData.last { it.hourlyWeather?.time != null }.hourlyWeather!!.time,
            utcOffset = hourlyChartData[0].utcOffset ?: 0L,
            timeClock = appSettings.widgetDateTime
        )!!
        val zone: ZoneId = when (appSettings.widgetDateTime) {
            HourlyDiagramTime.UTC -> ZoneId.of("UTC")
            HourlyDiagramTime.DEVICE -> ZoneId.systemDefault()
            HourlyDiagramTime.LOCATION -> {
                ZoneOffset.ofTotalSeconds((hourlyChartData.getOrNull(0)?.utcOffset?.toInt() ?: 0) * 60)
            }
        }

        val now: LocalDateTime = LocalDateTime.now(zone)


        val currentHourlyWeather = hourlyChartData.find {
            it.hourlyWeather?.time == LocalDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.HOURS).toString()
        }?.hourlyWeather
        val weatherType = WeatherType.fromWMO(
            code = currentHourlyWeather?.weatherCode,
            isDay = currentHourlyWeather?.isDay != 0,
            moonType = calculateMoonType(now.toLocalDate())
        )
        val weatherTypeSize = (48.dp).toPx(density).toInt()
        val weatherView = WeatherView(context, weatherType).apply {
            val widthSpec = View.MeasureSpec.makeMeasureSpec(
                weatherTypeSize,
                View.MeasureSpec.EXACTLY
            )
            val heightSpec = View.MeasureSpec.makeMeasureSpec(
                weatherTypeSize,
                View.MeasureSpec.EXACTLY
            )
            measure(widthSpec, heightSpec)
            layout(0, 0, measuredWidth, measuredHeight)
        }
        val weatherTypeBitmap = renderViewToBitmap(weatherView)

        val indicatorX = Duration.between(now.truncatedTo(ChronoUnit.HOURS), now).toMinutes() / 60f
        val currentTemperature = weatherCurveXtoY(
            quantity = HourlyWeatherQuantity.TEMPERATURE,
            appSettings = appSettings,
            hourlyWeather = hourlyChartData.map { it.hourlyWeather },
            hourlyAirQuality = null,
            firstPointX = firstPointX,
            lastPointX = lastPointX,
            targetX = indicatorX
        )?.roundToInt()
//        val currentWindSpeed = weatherCurveXtoY(
//            quantity = WeatherQuantity.WINDSPEED,
//            appSettings = appSettings,
//            hourlyWeather = hourlyChartData.map { it.hourlyWeather },
//            hourlyAirQuality = null,
//            firstPointX = firstPointX,
//            lastPointX = lastPointX,
//            targetX = indicatorX
//        )?.roundToInt()

        val todayData = dailyChartData.find { it.time == now.toLocalDate().toString() }
        val maxTemperature = todayData?.temperatureMax?.roundToInt()
        val minTemperature = todayData?.temperatureMin?.roundToInt()


        Box(
            modifier = GlanceModifier.fillMaxSize(),
            contentAlignment = Alignment.TopEnd
        ) {
            Column(
                modifier = GlanceModifier/*.padding(top = 24.dp)*/.fillMaxSize()
            ) {
                val dayOfWeek: String = now.dayOfWeek.getDisplayName(
                    java.time.format.TextStyle.FULL,
                    Locale.getDefault()
                )
                val clockText: String = now.format(DateTimeFormatter.ofPattern("HH\u202F:\u202Fmm"))
                TitleBar(
                    context = context,
                    id = id,
                    appSettings = appSettings,
                    locationName = hourlyChartData.getOrNull(0)?.locationName ?: "",
                    date = "$dayOfWeek, $clockText",
                    narrow = layoutWidth < MEDIUM_SQUARE.width.value.toInt(),
                    weatherTypeBitmap = weatherTypeBitmap,
                    weatherType = weatherType
                )
                Spacer(GlanceModifier.height(2.dp))
                Column(
                    modifier = GlanceModifier.fillMaxSize().padding(horizontal = 12.dp)
                        .background(
                            imageProvider = ImageProvider(R.drawable.widget_background),
                            colorFilter = ColorFilter.tint(
                                ColorProvider(
                                    day = colorScheme.surface,
                                    night = colorScheme.surface
                                )
                            )
                        )
                ) {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = GlanceModifier,
                            verticalAlignment = Alignment.Top,
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                modifier = GlanceModifier.width(96.dp),
                                text = "${currentTemperature?.toString() ?: "?"}°",
                                style = TextStyle(
                                    color = ColorProvider(
                                        day = colorScheme.onSurface,
                                        night = colorScheme.onSurface
                                    ),
                                    textAlign = TextAlign.Start,
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.SansSerif
                                )
                            )
                            Row(
                                modifier = GlanceModifier,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${minTemperature?.toString() ?: "?"}°",
                                    style = TextStyle(
                                        color = ColorProvider(
                                            day = BarBlue,
                                            night = BarBlueLight
                                        ),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                                Text(
                                    text = " / ",
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                )
//                                Image(
//                                    modifier = GlanceModifier.size(16.dp),
//                                    provider = ImageProvider(R.drawable.south_24),
//                                    contentDescription = null,
//                                    colorFilter = ColorFilter.tint(
//                                        colorProvider = ColorProvider(
//                                            day = BarBlue,
//                                            night = BarBlueLight
//                                        )
//                                    )
//                                )
//                                Image(
//                                    modifier = GlanceModifier.size(16.dp),
//                                    provider = ImageProvider(R.drawable.north_24),
//                                    contentDescription = null,
//                                    colorFilter = ColorFilter.tint(
//                                        colorProvider = ColorProvider(
//                                            day = Color.Red,
//                                            night = Color.Red
//                                        )
//                                    )
//                                )
                                Text(
                                    text = "${maxTemperature?.toString() ?: "?"}°",
                                    style = TextStyle(
                                        color = ColorProvider(
                                            day = Color.Red,
                                            night = Color.Red
                                        ),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                        Spacer(GlanceModifier.defaultWeight())
                        if (layoutWidth > SMALL_SQUARE.width.value.toInt()) {
                            HourlyPart(
                                context = context,
                                layoutWidth = layoutWidth,
                                now = now,
                                hourlyChartData = hourlyChartData,
                                appSettings = appSettings,
                                firstPointX = firstPointX,
                                lastPointX = lastPointX
                            )
                        } else {
                            Column(
                                modifier = GlanceModifier,
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalAlignment = Alignment.End
                            ) {
                                Spacer(GlanceModifier.height(16.dp))
                                Image(
                                    modifier = GlanceModifier.size(48.dp),
                                    provider = ImageProvider(weatherTypeBitmap!!),
                                    contentDescription = weatherType.weatherDesc
                                )
                            }
                        }
                    }
                    Spacer(GlanceModifier.defaultWeight())
                    if (layoutHeight > SMALL_SQUARE.height.value.toInt()) {
                        DailyPart(
                            context = context,
                            layoutWidth = layoutWidth,
                            layoutHeight = layoutHeight,
                            id = id,
                            now = now,
                            appSettings = appSettings,
                            dailyChartData = dailyChartData
                        )
                        Spacer(GlanceModifier.height(12.dp))
                    }
                }
            }
        }
    }


    @Composable
    fun TitleBar(
        context: Context,
        id: GlanceId,
        appSettings: AppSettings,
        locationName: String,
        date: String,
        narrow: Boolean,
        weatherTypeBitmap: Bitmap?,
        weatherType: WeatherType
    ) {
        val coroutineScope = rememberCoroutineScope()
        val colorScheme = getColorScheme(
            context = context,
            dynamicColor = appSettings.dynamicColor
        )

        Row(
            modifier = GlanceModifier.fillMaxWidth().height(64.dp)
                .background(
                    imageProvider = ImageProvider(R.drawable.widget_row_background),
                    colorFilter = ColorFilter.tint(
                        ColorProvider(
                            day = colorScheme.primaryContainer,
                            night = colorScheme.primaryContainer
                        )
                    )
                )
                .padding(horizontal = 12.dp)
                .clickable {
                    coroutineScope.launch {
                        update(context, id)
                    }
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = GlanceModifier.defaultWeight()
            ) {
                Text(
                    modifier = GlanceModifier.height(24.dp),
                    text = locationName,
                    style = TextStyle(
                        color = ColorProvider(day = colorScheme.primary, night = colorScheme.primary),
                        textAlign = TextAlign.Start,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = date,
                        style = TextStyle(
                            color = ColorProvider(day = Gray40, night = Gray20),
                            textAlign = TextAlign.Start,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )
                }
            }
            if (!narrow) {
                Image(
                    modifier = GlanceModifier.size(56.dp),
                    provider = ImageProvider(weatherTypeBitmap!!),
                    contentDescription = weatherType.weatherDesc
                )
            }
        }
    }

    @Composable
    fun HourlyPart(
        context: Context,
        layoutWidth: Int,
        now: LocalDateTime,
        hourlyChartData: List<HourlyChartDto>,
        appSettings: AppSettings,
        firstPointX: Float,
        lastPointX: Float,
    ) {
        val density = Density(context)
//        val size = LocalSize.current
        val colorScheme = getColorScheme(
            context = context,
            dynamicColor = appSettings.dynamicColor
        )

        val hoursList = List(
            size = when {
                layoutWidth > WIDE_RECTANGLE.width.value -> 4
                layoutWidth > HORIZONTAL_RECTANGLE.width.value -> 2
                else -> 1
            }
        ) { i -> now.plusHours((i + 1).toLong()).truncatedTo(ChronoUnit.HOURS) }

        val hourlyWeatherList = List(size = hoursList.size) { i ->
            hourlyChartData.find {
                it.hourlyWeather?.time == LocalDateTime.now(ZoneId.of("UTC"))
                    .plusHours((i + 1).toLong())
                    .truncatedTo(ChronoUnit.HOURS).toString()
            }
        }
        val weatherSize = 32.dp

        Row(
            modifier = GlanceModifier,
            horizontalAlignment = Alignment.End
        ) {
            hoursList.forEachIndexed { i, hour ->
                val indicatorX = Duration.between(
                    now.truncatedTo(ChronoUnit.HOURS),
                    hour
                ).toMinutes() / 60f
                val hourTemperature = weatherCurveXtoY(
                    quantity = HourlyWeatherQuantity.TEMPERATURE,
                    appSettings = appSettings,
                    hourlyWeather = hourlyChartData.map { it.hourlyWeather },
                    hourlyAirQuality = null,
                    firstPointX = firstPointX,
                    lastPointX = lastPointX,
                    targetX = indicatorX
                )?.roundToInt()

                val sunRise = hourlyWeatherList.getOrNull(i)?.sunRise?.let{ LocalDateTime.parse(it) }
                val sunSet = hourlyWeatherList.getOrNull(i)?.sunSet?.let{ LocalDateTime.parse(it) }
                val weatherType = WeatherType.fromWMO(
                    code = hourlyWeatherList.getOrNull(i)?.hourlyWeather?.weatherCode,
                    isDay = hour.isAfter(sunRise) && hour.isBefore(sunSet),
                    moonType = calculateMoonType(now.toLocalDate())
                )
                val weatherView = WeatherView(context, weatherType).apply {
                    val widthSpec = View.MeasureSpec.makeMeasureSpec(
                        weatherSize.toPx(density).toInt(),
                        View.MeasureSpec.EXACTLY
                    )
                    val heightSpec = View.MeasureSpec.makeMeasureSpec(
                        weatherSize.toPx(density).toInt(),
                        View.MeasureSpec.EXACTLY
                    )
                    measure(widthSpec, heightSpec)
                    layout(0, 0, measuredWidth, measuredHeight)
                }
                val weatherIconBitmap = renderViewToBitmap(weatherView)

                Spacer(GlanceModifier.width(8.dp))
                Column(
                    modifier = GlanceModifier.padding(top = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        modifier = GlanceModifier
                            .background(
                                imageProvider = ImageProvider(R.drawable.widget_text_background),
                                colorFilter = ColorFilter.tint(
                                    ColorProvider(
                                        day = colorScheme.primary,
                                        night = colorScheme.primary
                                    )
                                )
                            )
                            .padding(4.dp),
                        text = hour.format(DateTimeFormatter.ofPattern("hh a")),
                        style = TextStyle(
                            color = ColorProvider(
                                day = colorScheme.onPrimary,
                                night = colorScheme.onPrimary
                            ),
//                                                textAlign = TextAlign.Start,
                            fontSize = 14.sp,
                        )
                    )
                    Spacer(GlanceModifier.height(4.dp))
                    Text(
                        modifier = GlanceModifier,
                        text = "${hourTemperature?.toString() ?: "?"}°",
                        style = TextStyle(
                            color = ColorProvider(
                                day = colorScheme.onSurface,
                                night = colorScheme.onSurface
                            ),
                            textAlign = TextAlign.Start,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif
                        )
                    )
                    Spacer(GlanceModifier.height(4.dp))
                    Image(
                        modifier = GlanceModifier.size(weatherSize),
                        provider = ImageProvider(weatherIconBitmap!!),
                        contentDescription = weatherType.weatherDesc
                    )
                }
            }
        }
    }

    @Composable
    fun DailyPart(
        context: Context,
        layoutWidth: Int,
        layoutHeight: Int,
        id: GlanceId,
        now: LocalDateTime,
        appSettings: AppSettings,
        dailyChartData: List<LocalDailyWeather>,
    ) {
//        val size = LocalSize.current
        val colorScheme = getColorScheme(
            context = context,
            dynamicColor = appSettings.dynamicColor
        )
        val density = Density(context)
        var weather = currentState(weatherKey) ?: 0
        val allowedWeatherQuantities = DailyWeatherQuantity.entries
            .filter { it.historicalState != HistoricalState.HISTORIC_ONLY }
        val weatherQuantity = allowedWeatherQuantities.getOrNull(weather)
            ?: DailyWeatherQuantity.TEMPERATUREMINMAX
        val coroutineScope = rememberCoroutineScope()

        val daysList = List(size = 7) { i ->
            now.toLocalDate().plusDays((i + 1).toLong()).dayOfWeek
                .getDisplayName(
                    if (layoutWidth > SMALL_SQUARE.width.value.toInt()) java.time.format.TextStyle.FULL
                    else java.time.format.TextStyle.SHORT,
                    Locale.getDefault()
                )
        }
        val subDailyWeather = List(size = 7) { i ->
            dailyChartData.find {
                it.time == now.toLocalDate().plusDays((i + 1).toLong()).toString()
            }
        }
        val weatherSize = 32.dp
        val dayWeatherIconSize = 40.dp
        val dailyBoxIconOverlap = 16.dp
        val rowHeight = 40.dp

        Box(
            contentAlignment = Alignment.TopEnd
        ) {
            Column(
                modifier = GlanceModifier
            ) {
                Spacer(GlanceModifier.height(dayWeatherIconSize - dailyBoxIconOverlap))
                LazyColumn(
                    modifier = GlanceModifier.fillMaxWidth()
                        .height(
                            rowHeight * when {
                                layoutHeight > TALL_RECTANGLE.height.value.toInt() -> 7
                                layoutHeight > VERTICAL_RECTANGLE.height.value.toInt() -> 5
                                else -> 2
                            } + dailyBoxIconOverlap
                        )
                        .background(
                            imageProvider = ImageProvider(R.drawable.widget_day_part_background),
                            colorFilter = ColorFilter.tint(
                                ColorProvider(
                                    day = colorScheme.surfaceContainer,
                                    night = colorScheme.surfaceContainer
                                )
                            )
                        )
                        .cornerRadius(16.dp)
                ) {
                    item {
                        Spacer(GlanceModifier.height(dailyBoxIconOverlap))
                    }
                    items(
                        count = 7
                    ) {i ->
                        val weatherType = WeatherType.fromWMO(
                            code = subDailyWeather.getOrNull(i)?.weatherCode,
                            isDay = true,
                            moonType = null
                        )
                        val weatherView = WeatherView(context, weatherType).apply {
                            val widthSpec = View.MeasureSpec.makeMeasureSpec(
                                weatherSize.toPx(density).toInt(),
                                View.MeasureSpec.EXACTLY
                            )
                            val heightSpec = View.MeasureSpec.makeMeasureSpec(
                                weatherSize.toPx(density).toInt(),
                                View.MeasureSpec.EXACTLY
                            )
                            measure(widthSpec, heightSpec)
                            layout(0, 0, measuredWidth, measuredHeight)
                        }
                        val weatherIconBitmap = renderViewToBitmap(weatherView)

                        Row(
                            modifier = GlanceModifier.padding(horizontal = 8.dp)
                                .fillMaxWidth().height(rowHeight),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                modifier = GlanceModifier.defaultWeight(),
                                text = daysList[i],
                                style = TextStyle(
                                    color = ColorProvider(
                                        day = colorScheme.onSurface,
                                        night = colorScheme.onSurface
                                    ),
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Start
                                )
                            )
                            if (layoutWidth > SMALL_SQUARE.width.value.toInt()) {
                                Image(
                                    modifier = GlanceModifier.size(weatherSize),
                                    provider = ImageProvider(weatherIconBitmap!!),
                                    contentDescription = weatherType.weatherDesc
                                )
                            }

                            DailyWeatherValue(
                                modifier = GlanceModifier.defaultWeight(),
                                subDailyWeather = subDailyWeather.getOrNull(i),
                                quantity = weatherQuantity,
                                appSettings = appSettings
                            )

                        }
                    }
                }
            }
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Box(
                    modifier = GlanceModifier
                        .background(
                            imageProvider = ImageProvider(R.drawable.widget_circle_border),
                            colorFilter = ColorFilter.tint(
                                ColorProvider(
                                    day = colorScheme.surface,
                                    night = colorScheme.surface
                                )
                            )
                        )
                        .padding(2.dp)
                ) {
                    Image(
                        modifier = GlanceModifier.size(dayWeatherIconSize).padding(8.dp)
                            .background(
                                imageProvider = ImageProvider(R.drawable.widget_circle),
                                colorFilter = ColorFilter.tint(
                                    ColorProvider(
                                        day = colorScheme.surfaceContainer,
                                        night = colorScheme.surfaceContainer
                                    )
                                )
                            )
                            .clickable(
                                rippleOverride = R.drawable.widget_ripple
                            ) {
                                weather = (weather + 1) % allowedWeatherQuantities.size

                                coroutineScope.launch {
                                    updateAppWidgetState(context, id) { pref ->
                                        pref[weatherKey] = weather
                                    }
                                    update(context, id)
                                }
                            },
                        provider = ImageProvider(weatherQuantity.iconId),
                        contentDescription = ""
                    )
                }

                Spacer(GlanceModifier.width(16.dp))
            }
        }
    }

    @Composable
    fun DailyWeatherValue(
        modifier: GlanceModifier,
        subDailyWeather: LocalDailyWeather?,
        quantity: DailyWeatherQuantity,
        appSettings: AppSettings
    ) {
        val text = when(quantity) {
            DailyWeatherQuantity.SUNRISESET ->
                "${subDailyWeather?.sunRise?.substringAfterLast('T')}\n${subDailyWeather?.sunSet?.substringAfterLast('T')}"
            DailyWeatherQuantity.UVINDEXMAX ->
                "${quantity.quantityValueText(subDailyWeather?.uvIndexMax, appSettings)}"
            DailyWeatherQuantity.PRECIPITATIONSUM ->
                "${quantity.quantityValueText(subDailyWeather?.precipitationSum, appSettings)}"
            DailyWeatherQuantity.PRECIPITATIONPROBABILITYMAX ->
                "${quantity.quantityValueText(subDailyWeather?.precipitationProbabilityMax, appSettings)}"
            DailyWeatherQuantity.WINDSPEEDMAX ->
                "${subDailyWeather?.windSpeedMax?.roundToInt()?.toString() ?: "?"} ${quantity.unit(appSettings)}"

            else -> ""
        }

        when (quantity){
            DailyWeatherQuantity.TEMPERATUREMINMAX -> {
                Row(
                    modifier = modifier,
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        modifier = GlanceModifier,
                        text = "${subDailyWeather?.temperatureMin?.roundToInt()?.toString() ?: "?"}°",
                        style = TextStyle(
                            color = ColorProvider(
                                day = BarBlue,
                                night = BarBlueLight
                            ),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = " / ",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal
                        )
                    )
                    Text(
                        modifier = GlanceModifier,
                        text = "${subDailyWeather?.temperatureMax?.roundToInt()?.toString() ?: "?"}°",
                        style = TextStyle(
                            color = ColorProvider(
                                day = Color.Red,
                                night = Color.Red
                            ),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
            else -> {
                Text(
                    modifier = modifier,
                    text = text,
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End
                    )
                )
            }
        }



    }

}
