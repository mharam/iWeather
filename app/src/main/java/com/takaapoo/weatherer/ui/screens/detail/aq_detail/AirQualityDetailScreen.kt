package com.takaapoo.weatherer.ui.screens.detail.aq_detail

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.DetailState
import com.takaapoo.weatherer.ui.screens.detail.AirQualityCurrentCondition2
import com.takaapoo.weatherer.ui.screens.detail.SingleConnector4
import com.takaapoo.weatherer.ui.screens.detail.WeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.linearCurveXtoY
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.quantityData
import com.takaapoo.weatherer.ui.theme.AQGreen
import com.takaapoo.weatherer.ui.theme.AQMaroon
import com.takaapoo.weatherer.ui.theme.AQOrange
import com.takaapoo.weatherer.ui.theme.AQPurple
import com.takaapoo.weatherer.ui.theme.AQRed
import com.takaapoo.weatherer.ui.theme.AQYellow
import com.takaapoo.weatherer.ui.theme.WeathererTheme
import com.takaapoo.weatherer.ui.theme.customColorScheme
import com.takaapoo.weatherer.ui.utility.BorderedText
import com.takaapoo.weatherer.ui.utility.toCelsius
import com.takaapoo.weatherer.ui.utility.toPa
import com.takaapoo.weatherer.ui.utility.toSp
import com.takaapoo.weatherer.ui.viewModels.timeFontFamily
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun AirQualityDetailScreen(
    modifier: Modifier = Modifier,
    detailState: DetailState = DetailState(),
    location: Location,
    appSettings: AppSettings = AppSettings(),
//    sharedTransitionScope: SharedTransitionScope,
//    animatedContentScope: AnimatedContentScope
    onNavigateUp: () -> Unit
) {
    val density = LocalDensity.current

    val infiniteTransition = rememberInfiniteTransition(label = "clock")
    val clockColonVisibility by infiniteTransition.animateValue(
        initialValue = 0f,
        targetValue = 1f,
        typeConverter = Float.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "clock colon"
    )

    val firstPointX = 0f
    val lastPointX = detailState.currentDayHourlyAirQuality.size.toFloat() - 1
    val temperatureData = quantityData(
        weatherData = detailState.currentDayHourlyWeather,
        airData = detailState.currentDayHourlyAirQuality,
        weatherQuantity = WeatherQuantity.TEMPERATURE
    )
    val pressureData = quantityData(
        weatherData = detailState.currentDayHourlyWeather,
        airData = detailState.currentDayHourlyAirQuality,
        weatherQuantity = WeatherQuantity.SURFACEPRESSURE
    )
    val currentTemperature = linearCurveXtoY(
        data = temperatureData,
        firstPointX = firstPointX,
        lastPointX = lastPointX,
        targetX = detailState.targetX
    )?.toCelsius(appSettings.temperatureUnit)
    val currentPressure = linearCurveXtoY(
        data = pressureData,
        firstPointX = firstPointX,
        lastPointX = lastPointX,
        targetX = detailState.targetX
    )?.toPa(appSettings.pressureUnit)

    val layoutDirection = LocalLayoutDirection.current
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer),
                title = {
                    Text(
                        text = location.name,
                        fontFamily = Font(R.font.cmu_serif_bold).toFontFamily(),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    Text(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        text = if (clockColonVisibility <= 0.5f)
                            detailState.localDateTime.toLocalTime()
                                .format(DateTimeFormatter.ofPattern("HH\u200C:\u200Cmm"))
                        else detailState.localDateTime.toLocalTime()
                            .format(DateTimeFormatter.ofPattern("HH\u200C \u200Cmm")),
                        fontFamily = timeFontFamily
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0x00FFFFFF)
                ),
                windowInsets = WindowInsets(
                    left = 0, top = TopAppBarDefaults.windowInsets.getTop(density), right = 0, bottom = 0
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    start = 8.dp,
                    end = 8.dp
                )
                .background(color = MaterialTheme.customColorScheme.detailScreenSurface)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = dimensionResource(id = R.dimen.detail_screen_subtitle_vertical_padding))
                    .zIndex(1f)
                    .align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier
                        .height(dimensionResource(id = R.dimen.detail_screen_subtitle_height))
                        .graphicsLayer {
                            shape = RoundedCornerShape(percent = 50)
                            shadowElevation = 8.dp.toPx()
                            clip = true
                        }
                        .background(color = MaterialTheme.colorScheme.tertiaryContainer)
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Current Air Quality Condition",
                        fontFamily = Font(R.font.cmu_typewriter_bold).toFontFamily(),
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontSize = dimensionResource(id = R.dimen.detail_screen_subtitle_font_size).toSp(density)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(state = rememberScrollState())
            ) {
                Spacer(
                    modifier = Modifier.height(
                        dimensionResource(id = R.dimen.detail_screen_subtitle_height) +
                                dimensionResource(id = R.dimen.detail_screen_subtitle_vertical_padding)
                    )
                )
//                with(sharedTransitionScope) {
                    AirQualityCurrentCondition2(
                        modifier = Modifier.Companion
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                        /*.sharedBounds(
                            rememberSharedContentState(key = "AQI_${location.id}"),
                            animatedVisibilityScope = animatedContentScope,
                        )*/,
                        detailState = detailState,
                        quantity = WeatherQuantity.AQI,
                    )
                    AirQualityCurrentCondition2(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        detailState = detailState,
                        quantity = WeatherQuantity.PM10,
                    )
                    AirQualityCurrentCondition2(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        detailState = detailState,
                        quantity = WeatherQuantity.PM2_5,
                    )
                    AirQualityCurrentCondition2(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        detailState = detailState,
                        quantity = WeatherQuantity.CO,
                        currentTemperature = 273 + (currentTemperature ?: 0f),
                        currentPressure = currentPressure ?: 1000f
                    )
                    AirQualityCurrentCondition2(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        detailState = detailState,
                        quantity = WeatherQuantity.NO2,
                        currentTemperature = 273 + (currentTemperature ?: 0f),
                        currentPressure = currentPressure ?: 1000f
                    )
                    AirQualityCurrentCondition2(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        detailState = detailState,
                        quantity = WeatherQuantity.SO2,
                        currentTemperature = 273 + (currentTemperature ?: 0f),
                        currentPressure = currentPressure ?: 1000f
                    )
                    AirQualityCurrentCondition2(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        detailState = detailState,
                        quantity = WeatherQuantity.Ozone,
                        currentTemperature = 273 + (currentTemperature ?: 0f),
                        currentPressure = currentPressure ?: 1000f
                    )
//                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.Top
                ){
                    Text(
                        modifier = Modifier.width(16.dp),
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    fontSize = 14.sp,
                                    baselineShift = BaselineShift.Superscript
                                )
                            ) {
                                append("✻")
                            }
                        },
                        textAlign = TextAlign.Center
                    )
                    Text(text = ": ")
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        text = concentration(micro = true),
                        textAlign = TextAlign.Left
                    )
                }
                Row(
                    modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.Top
                ){
                    Text(
                        modifier = Modifier.width(16.dp),
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    fontSize = 14.sp,
                                    baselineShift = BaselineShift.Superscript
                                )
                            ) {
                                append("★")
                            }
                        },
                        textAlign = TextAlign.Center
                    )
                    Text(text = ": ")
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        text = concentration(micro = false),
                        textAlign = TextAlign.Left
                    )
                }
                Row(
                    modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.Top
                ){
                    Text(
                        modifier = Modifier.width(16.dp),
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    fontSize = 14.sp,
                                    baselineShift = BaselineShift.Superscript
                                )
                            ) {
                                append("†")
                            }
                        },
                        textAlign = TextAlign.Center
                    )
                    Text(text = ": ")
                    Text(
                        modifier = Modifier.fillMaxWidth()
                            .padding(end = paddingValues.calculateEndPadding(layoutDirection))
                        ,
                        text = buildAnnotatedString {
                            append("Particulate matter with diameter smaller than 10 ")
                            withStyle(
                                style = SpanStyle(
                                    fontFamily = Font(R.font.cmu_serif_italic).toFontFamily()
                                )
                            ){ append("µ") }
                            append("m (PM")
                            withStyle(
                                style = SpanStyle(
                                    fontSize = 12.sp,
                                    baselineShift = BaselineShift.Subscript
                                )
                            ){ append("10") }
                            append(") and smaller than 2.5 ")
                            withStyle(
                                style = SpanStyle(
                                    fontFamily = Font(R.font.cmu_serif_italic).toFontFamily()
                                )
                            ){ append("µ") }
                            append("m (PM")
                            withStyle(
                                style = SpanStyle(
                                    fontSize = 12.sp,
                                    baselineShift = BaselineShift.Subscript
                                )
                            ){ append("2.5") }
                            append(") close to surface (10 meter above ground)")
                        },
                        textAlign = TextAlign.Justify
                    )
                }
                Row(
                    modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.Top
                ){
                    Text(
                        modifier = Modifier.width(16.dp),
                        text = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    fontSize = 14.sp,
                                    baselineShift = BaselineShift.Superscript
                                )
                            ) {
                                append("‡")
                            }
                        },
                        textAlign = TextAlign.Center
                    )
                    Text(text = ": ")
                    Text(
                        modifier = Modifier.fillMaxWidth()
                            .padding(end = paddingValues.calculateEndPadding(layoutDirection)),
                        text = "Atmospheric gases close to surface (10 meter above ground)",
                        textAlign = TextAlign.Justify
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth()){
                    var topPoint by remember{ mutableStateOf(Offset.Zero) }
                    val bottomPoints = remember{
                        Array(6){ mutableStateOf(Offset.Zero) }
                    }
                    bottomPoints.forEach {
                        SingleConnector4(
                            modifier = Modifier.fillMaxSize(),
                            topPoint = topPoint,
                            bottomPoint = it.value
                        )
                    }
                    Column(
                        modifier = Modifier.width(IntrinsicSize.Max)
                            .align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val qualities = stringArrayResource(id = R.array.air_quality_description)
                        val colors = listOf(AQGreen, AQYellow, AQOrange, AQRed, AQPurple, AQMaroon)
                        Text(
                            modifier = Modifier
                                .padding(8.dp)
                                .onGloballyPositioned {
                                    topPoint = (it.parentLayoutCoordinates?.positionInParent() ?: Offset.Zero) +
                                        it.positionInParent() + Offset(0f, it.size.height / 2f)
                                }
                                .defaultMinSize(
                                    minHeight = dimensionResource(id = R.dimen.current_condition_param_height)
                                )
                                .background(
                                    color = MaterialTheme.customColorScheme.detailScreenSurface,
                                    shape = RoundedCornerShape(percent = 50)
                                )
                                .border(
                                    width = dimensionResource(id = R.dimen.connector_line_width),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(percent = 50)
                                )
                                .padding(8.dp),
                            textAlign = TextAlign.Center,
                            text =  "Air Quality Levels",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        qualities.forEachIndexed { index, quality ->
                            AirQualityLevel(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 36.dp)
                                    .onGloballyPositioned {
                                        bottomPoints[index].value =
                                            (it.parentLayoutCoordinates?.positionInParent() ?: Offset.Zero) +
                                            it.positionInParent() +
                                            Offset(0f, it.size.height / 2f)
                                    },
                                color = colors[index],
                                text = quality
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
    BackHandler {
        onNavigateUp()
    }
}

@Composable
fun AirQualityLevel(
    modifier: Modifier = Modifier,
    color: Color,
    text: String
) {
    BorderedText(
        modifier = modifier
            .background(
                color = color,
                shape = RoundedCornerShape(percent = 50)
            )
            .border(
                width = dimensionResource(id = R.dimen.connector_line_width),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(percent = 50)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        text = AnnotatedString(text),
        fontSize = 16.sp,
        fillColor = Color.Black,
        strokeColor = Color.Black,
        strokeWidth = 1f,
        alignment = Alignment.Center
    )
}

fun concentration(micro: Boolean): AnnotatedString {
    return buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                fontFamily = Font(R.font.cmu_serif_italic).toFontFamily()
            )
        ){ append(if (micro) "µ" else "m") }
        append("\u200Cg")
        withStyle(
            style = SpanStyle(fontSize = 20.sp)
        ){ append("/")}
        append("m")
        withStyle(
            style = SpanStyle(
                fontSize = 12.sp,
                baselineShift = BaselineShift.Superscript
            )
        ){ append("3") }
    }
}


@Preview(showBackground = true)
@Composable
fun AirQualityDetailScreenPreview(modifier: Modifier = Modifier) {
    WeathererTheme {
        AirQualityDetailScreen(
            location = Location(),
            onNavigateUp = {}
        )
    }
}

