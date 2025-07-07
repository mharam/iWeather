package com.takaapoo.weatherer.ui.widget

import android.content.Context
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
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
import androidx.glance.layout.wrapContentHeight
import androidx.glance.text.FontFamily
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.data.local.LocalDailyWeather
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import com.takaapoo.weatherer.domain.repository.LocalWeatherRepository
import com.takaapoo.weatherer.domain.repository.PreferenceRepository
import com.takaapoo.weatherer.domain.use_case.GetHourlyChartDataUseCase
import com.takaapoo.weatherer.ui.notification.custom_views.timeToX
import com.takaapoo.weatherer.ui.screens.detail.WeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.weatherCurveXtoY
import com.takaapoo.weatherer.ui.theme.Transparent
import com.takaapoo.weatherer.utility.getColorScheme
import com.takaapoo.weatherer.utility.renderViewToBitmap
import com.takaapoo.weatherer.utility.toDp
import com.takaapoo.weatherer.utility.toPx
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit


class WeatherWidget1(): GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Exact
    val tabKey = intPreferencesKey("tab")

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val preferenceRepository = getHiltDependency<PreferenceRepository>(context)
        val localWeatherRepository = getHiltDependency<LocalWeatherRepository>(context)
        val hourlyChartDataUseCase = getHiltDependency<GetHourlyChartDataUseCase>(context)
        val appSettings = preferenceRepository.getSettings()
        val hourlyChartData = hourlyChartDataUseCase(
            locationId = appSettings.widgetLocationId,
            startDate = LocalDate.now(ZoneId.of("UTC")).minusDays(1).toString(),
            endDate = LocalDate.now(ZoneId.of("UTC")).plusDays(3).toString(),
            appSettings = appSettings
        )
        val dailyChartData = localWeatherRepository.getLocationDailyWeather(
            locationId = appSettings.widgetLocationId,
            startDate = LocalDate.now().toString(),
            endDate = LocalDate.now().plusDays(4).toString(),
            appSettings = appSettings
        )


        provideContent {
            MyContent(
                context = context,
                id = id,
                widgetHourlyChartData = hourlyChartData,
                widgetDailyChartData = dailyChartData,
                appSettings = appSettings,
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
        widgetHourlyChartData: List<HourlyChartDto>,
        widgetDailyChartData: List<LocalDailyWeather>,
        appSettings: AppSettings,
    ) {
        val density = Density(context)
        val size = LocalSize.current
        val coroutineScope = rememberCoroutineScope()
        var tab = currentState(tabKey) ?: 1
        val colorScheme = getColorScheme(
            context = context,
            dynamicColor = appSettings.dynamicColor
        )

        val background = BackgroundView(
            context = context,
            hourlyChartData = widgetHourlyChartData,
            dailyChartData = widgetDailyChartData,
            appSettings = appSettings,
            tab = tab
        ).apply {
            val widthSpec = View.MeasureSpec.makeMeasureSpec(
                size.width.toPx(density).toInt(),
                View.MeasureSpec.EXACTLY
            )
            val heightSpec = View.MeasureSpec.makeMeasureSpec(
                size.height.toPx(density).toInt(),
                View.MeasureSpec.EXACTLY
            )
            measure(widthSpec, heightSpec)
            layout(0, 0, measuredWidth, measuredHeight)
        }
        val backgroundBitmap = renderViewToBitmap(background)

        Column(
            modifier = GlanceModifier.fillMaxSize()
                .background(ImageProvider(backgroundBitmap!!)),
            verticalAlignment = Alignment.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val spaceHeight = (context.resources.getDimension(R.dimen.widget1_top_gap) +
                    background.topPartHeight).toDp(density)
            Spacer(GlanceModifier.height(spaceHeight))
            Box(
                modifier = GlanceModifier.fillMaxWidth().wrapContentHeight(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Current",
                        modifier = GlanceModifier.defaultWeight()
                            .padding(16.dp)
                            .clickable {
                                tab = 1
                                coroutineScope.launch {
                                    updateAppWidgetState(context, id) { pref ->
                                        pref[tabKey] = 1
                                    }
                                    update(context, id)
                                }
                            },
                        style = TextStyle(
                            color = if (tab == 1)
                                ColorProvider(
                                    day = colorScheme.onSurface,
                                    night = colorScheme.onSurface
                                )
                            else
                                ColorProvider(
                                    day = colorScheme.onSurfaceVariant,
                                    night = colorScheme.onSurfaceVariant
                                ),
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Hourly",
                        modifier = GlanceModifier.defaultWeight()
                            .padding(16.dp)
                            .clickable {
                                tab = 2
                                coroutineScope.launch {
                                    updateAppWidgetState(context, id) { pref ->
                                        pref[tabKey] = 2
                                    }
                                    update(context, id)
                                }
                            },
                        style = TextStyle(
                            color = if (tab == 2)
                                ColorProvider(
                                    day = colorScheme.onSurface,
                                    night = colorScheme.onSurface
                                )
                            else
                                ColorProvider(
                                    day = colorScheme.onSurfaceVariant,
                                    night = colorScheme.onSurfaceVariant
                                ),
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "Daily",
                        modifier = GlanceModifier.defaultWeight()
                            .padding(16.dp)
                            .clickable {
                                tab = 3
                                coroutineScope.launch {
                                    updateAppWidgetState(context, id) { pref ->
                                        pref[tabKey] = 3
                                    }
                                    update(context, id)
                                }
                            },
                        style = TextStyle(
                            color = if (tab == 3)
                                ColorProvider(
                                    day = colorScheme.onSurface,
                                    night = colorScheme.onSurface
                                )
                            else
                                ColorProvider(
                                    day = colorScheme.onSurfaceVariant,
                                    night = colorScheme.onSurfaceVariant
                                ),
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Column {
                    Row(
                        modifier = GlanceModifier.fillMaxWidth()
                    ){
                        Box(
                            modifier = GlanceModifier.height(2.dp).defaultWeight()
                                .background(
                                    if (tab == 1) colorScheme.primary
                                    else Transparent
                                )
                        ){}
                        Box(
                            modifier = GlanceModifier.height(2.dp).defaultWeight()
                                .background(
                                    if (tab == 2) colorScheme.primary
                                    else Transparent
                                )
                        ){}
                        Box(
                            modifier = GlanceModifier.height(2.dp).defaultWeight()
                                .background(
                                    if (tab == 3) colorScheme.primary
                                    else Transparent
                                )
                        ){}
                    }
                    Box(
                        modifier = GlanceModifier.height(1.dp).fillMaxWidth()
                            .background(colorScheme.outlineVariant)
                    ){}
                }
            }

            when (tab) {
                1 -> {
                    val firstPointX = timeToX(
                        time =  widgetHourlyChartData[0].hourlyWeather!!.time,
                        utcOffset = widgetHourlyChartData[0].utcOffset ?: 0L,
                        timeClock = appSettings.widgetDateTime
                    )!!
                    val lastPointX = timeToX(
                        time = widgetHourlyChartData.last { it.hourlyWeather?.time != null }
                            .hourlyWeather!!.time,
                        utcOffset = widgetHourlyChartData[0].utcOffset ?: 0L,
                        timeClock = appSettings.widgetDateTime
                    )!!
                    val now = LocalDateTime.now(ZoneId.of("UTC"))
                        .plusMinutes(widgetHourlyChartData.getOrNull(0)?.utcOffset ?: 0L)
                    val indicatorX = Duration.between(now.truncatedTo(ChronoUnit.HOURS), now).toMinutes() / 60f
                    val textStyle = TextStyle(
                        color =
                            ColorProvider(
                                day = colorScheme.onSurface,
                                night = colorScheme.onSurface
                            ),
                        fontSize = 14.sp,
                        fontFamily = FontFamily.SansSerif
                    )

                    LazyColumn(
                        modifier = GlanceModifier
                    ) {
                        item {
                            Spacer(GlanceModifier.height(8.dp))
                        }
                        items(
                            items = WeatherQuantity.entries.filter { it.homeCardNameId != null },
                            itemId = {
                                it.nameId!!.toLong()
                            }
                        ) {
                            val curveValueAtIndicator = weatherCurveXtoY(
                                quantity = it,
                                appSettings = appSettings,
                                hourlyWeather = widgetHourlyChartData.map { it.hourlyWeather },
                                hourlyAirQuality = widgetHourlyChartData.map { it.airQuality },
                                firstPointX = firstPointX,
                                lastPointX = lastPointX,
                                targetX = indicatorX
                            )

                            Row(
                                modifier = GlanceModifier.fillMaxWidth().height(32.dp)
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${context.getString(it.homeCardNameId!!)}:",
                                    style = textStyle
                                )
                                Spacer(
                                    modifier = GlanceModifier.defaultWeight()
                                )
                                Text(
                                    text = it.quantityValueText(
                                        quantityValue = curveValueAtIndicator,
                                        appSettings = appSettings,
                                    ).toString(),
                                    style = textStyle
                                )
                            }
                        }
                        item {
                            Spacer(GlanceModifier.height(8.dp))
                        }
                    }

                }
                else -> {

                }
            }
        }
    }
}