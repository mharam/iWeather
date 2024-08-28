package com.takaapoo.weatherer.ui.screens.detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.android.renderscript.Toolkit
import com.takaapoo.weatherer.MainActivity
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.data.Screens
import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.ChartState
import com.takaapoo.weatherer.domain.model.DailyChartState
import com.takaapoo.weatherer.domain.model.DetailState
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.cubicCurveXtoY
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.quantityControlPoints
import com.takaapoo.weatherer.ui.screens.home.toPx
import com.takaapoo.weatherer.ui.theme.Transparent
import com.takaapoo.weatherer.ui.theme.WeathererTheme
import com.takaapoo.weatherer.ui.viewModels.DetailViewModel
import com.takaapoo.weatherer.ui.viewModels.timeFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

var detailScreenWidth = 0
const val LAST_PAGE_NUMBER = "last_page_number"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun DetailPagerScreen(
    modifier: Modifier = Modifier,
    roomWidth: Float? = 0f,
    navController: NavController = rememberNavController(),
    detailViewModel: DetailViewModel = hiltViewModel(),
    chartState: ChartState = ChartState(),
    dailyChartState: DailyChartState = DailyChartState(),
    pageNumber: Int = 0,
    appSettings: AppSettings = AppSettings(),
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedContentScope
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.getTop(density)
    val spaceAboveTitle = 1f * (context as MainActivity).windowHeight
    val titleInitialScroll = 0.3f * spaceAboveTitle
    val headerHeight = (64.dp).toPx(density) + statusBarHeight
    val temperatureTopSpacerHeight = (0.2f * context.windowHeight).toDp(density)

    val detailState by detailViewModel.detailState.map { it[pageNumber] }
        .collectAsStateWithLifecycle(initialValue = DetailState())
    val hourlyChartData by detailViewModel.locationsHourlyChartData.getOrElse(
        index = pageNumber,
        defaultValue = { flowOf(emptyList()) }
    ).collectAsStateWithLifecycle(initialValue = emptyList())

    val dailyChartData by detailViewModel.locationsDailyChartData.getOrElse(
        index = pageNumber,
        defaultValue = { flowOf(emptyList()) }
    ).collectAsStateWithLifecycle(initialValue = emptyList())

    val location = detailViewModel.allLocations.getOrElse(
        index = pageNumber,
        defaultValue = { Location() }
    )
    val coroutineScope = rememberCoroutineScope()


    val scrollState = rememberScrollState(initial = detailState.scrollValue)
    val titleCornerRadius = ((spaceAboveTitle - scrollState.value)/headerHeight)
        .coerceIn(0f,1f) * 32.dp
    val temperatureY = when {
        scrollState.value < titleInitialScroll -> (scrollState.value - titleInitialScroll) / 5
        scrollState.value < 1.2f * titleInitialScroll -> 0f
        else -> -scrollState.value + 1.2f * titleInitialScroll
    }
    val temperatureAlpha = (scrollState.value.toFloat() / titleInitialScroll)
        .coerceAtMost(1f)
    val showTemperature by remember {
        derivedStateOf { scrollState.value < context.windowHeight }
    }

    val imageBitmap  by remember {
        mutableStateOf(ContextCompat.getDrawable(
            context,
            when(pageNumber){
                0 -> R.drawable.ax2_c
                1 -> R.drawable.ax3_c
                else -> R.drawable.ax4_c
            }
        )!!.toBitmap())
//            ImageBitmap.imageResource(R.drawable.ax2).asAndroidBitmap()
    }
    val blurredImage by remember {
        mutableStateOf(Toolkit.blur(inputBitmap = imageBitmap, radius = 3).asImageBitmap())
    }

    LaunchedEffect(scrollState.isScrollInProgress){
        if (!scrollState.isScrollInProgress &&
            scrollState.value.toFloat() in spaceAboveTitle - statusBarHeight .. spaceAboveTitle){
            scrollState.animateScrollTo(
                value = spaceAboveTitle.toInt(),
                animationSpec = tween(durationMillis = 300)
            )
        }
    }
    LaunchedEffect(Unit){
        delay(100)
        launch {
            if (detailState.scrollValue == 0) {
                scrollState.animateScrollTo(
                    value = titleInitialScroll.toInt(),
                    animationSpec = tween(durationMillis = 500)
                )
            }
        }
    }
    LaunchedEffect(key1 = detailState.navigateBack) {
        if (detailState.navigateBack){
            scrollState.animateScrollTo(
                value = 0,
                animationSpec = tween(durationMillis = 500)
            )
            navController.previousBackStackEntry?.savedStateHandle?.set(LAST_PAGE_NUMBER, pageNumber)
            navController.navigateUp()
        }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        detailViewModel.updateScrollValue(value = scrollState.value, pageIndex = pageNumber)
    }


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
    val subtitleVerticalSpace = (dimensionResource(id = R.dimen.detail_screen_subtitle_height) +
            dimensionResource(id = R.dimen.detail_screen_subtitle_vertical_padding) * 2).toPx(density)
    val timeAlpha = when {
        scrollState.value < spaceAboveTitle -> 1f
        else -> ((spaceAboveTitle + subtitleVerticalSpace - scrollState.value)/subtitleVerticalSpace)
            .coerceAtLeast(0f)
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.background(
                    if (showTemperature) Transparent
                    else MaterialTheme.colorScheme.primaryContainer
                ),
                title = {
                    Text(
                        text = if (showTemperature) "" else  location.name,
                        fontFamily = Font(R.font.cmu_serif_bold).toFontFamily(),
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        detailViewModel.updateNavigateBack(
                            state = true,
                            pageIndex = pageNumber
                        )
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (timeAlpha < 1f) {
                        Text(
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .alpha(1 - timeAlpha),
                            text = if (clockColonVisibility <= 0.5f)
                                detailState.localDateTime.toLocalTime()
                                    .format(DateTimeFormatter.ofPattern("HH\u200C:\u200Cmm"))
                            else detailState.localDateTime.toLocalTime()
                                .format(DateTimeFormatter.ofPattern("HH\u200C \u200Cmm")),
                            fontFamily = timeFontFamily
                        )
                    }
//                    HourlyDiagramSettingsOptions(
//                        settingsOpen = detailState.hourlyDiagramSettingOpen,
//                        pageNumber = pageNumber,
//                        chartState = chartState,
//                        onUpdateHourlyDiagramSettingOpen = detailViewModel::updateHourlyDiagramSettingOpen,
//                        onUpdateChartCurveShadowVisibility = detailViewModel::updateHourlyChartCurveShadowVisibility,
//                        onUpdateChartDotsOnCurveVisibility = detailViewModel::updateHourlyChartDotsOnCurveVisibility,
//                        onUpdateChartWeatherConditionVisibility = detailViewModel::updateHourlyChartWeatherIconVisibility,
//                        onUpdateChartSunRiseSetIconsVisibility = detailViewModel::updateHourlyChartSunRiseSetIconsVisibility,
//                        onUpdateChartGrids = detailViewModel::updateHourlyChartGrid
//                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0x00FFFFFF)
                )
            )
        }
    ) { paddingValues ->
        AnimatedVisibility (detailState.chooseDiagramThemeDialogVisible) {
            ChooseDiagramThemeDialog(
                onConfirmation = {
                    detailViewModel.updateChooseDiagramThemeDialogVisibility(
                        visible = false,
                        pageIndex = pageNumber
                    )
                },
                chartTheme = chartState.chartTheme,
                onSelectTheme = detailViewModel::updateHourlyChartTheme
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    detailScreenWidth = it.size.width
                }
        ) {
            if (showTemperature) {
                val axScale = 3.3f * roomWidth!! / context.windowHeight
                Image(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = axScale
                            scaleY = axScale
                        },
                    painter = painterResource(
                        when (pageNumber) {
                            0 -> R.drawable.ax2
                            1 -> R.drawable.ax3
                            else -> R.drawable.ax4
                        }
                    ),
                    contentDescription = null,
                    contentScale = ContentScale.FillHeight
                )
                val scale = 0.8f + 0.2f * temperatureAlpha
                Temperatures(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            translationY = temperatureY
                            alpha = temperatureAlpha
                            scaleX = scale
                            scaleY = scale
                            TransformOrigin(0.5f, 0f)
                        },
                    detailState = detailState,
                    topSpacerHeight = temperatureTopSpacerHeight
                )
                Image(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithContent {
                            clipPath(
                                path = Path().apply {
                                    addRoundRect(
                                        RoundRect(
                                            rect = Rect(
                                                offset = Offset(
                                                    0f,
                                                    spaceAboveTitle - scrollState.value
                                                ),
                                                size = Size(detailScreenWidth.toFloat(), 5000f)
                                            ),
                                            cornerRadius = CornerRadius(
                                                titleCornerRadius.toPx(),
                                                titleCornerRadius.toPx()
                                            )
                                        )
                                    )
                                }
                            ) {
                                this@drawWithContent.drawContent()
                            }
                        }
                        .graphicsLayer {
                            scaleX = axScale
                            scaleY = axScale
                        },
                    bitmap = blurredImage,
                    contentDescription = null,
                    contentScale = ContentScale.FillHeight
                )
            }

//            Image(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .drawWithContent {
//                        clipPath(
//                            path = Path().apply {
//                                addRoundRect(
//                                    RoundRect(
//                                        rect = Rect(
//                                            offset = Offset(0f, detailState.headerTopPosition),
//                                            size = Size(detailScreenWidth.toFloat(), 5000f)
//                                        ),
//                                        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
//                                    )
//                                )
//                            }
//                        ){
//                            this@drawWithContent.drawContent()
//                        }
//                    }
//                    .blur(
//                        radius = 4.dp,
//                        edgeTreatment = BlurredEdgeTreatment.Rectangle
//                    ),
//                painter = painterResource(R.drawable.ax2_c),
//                contentDescription = null,
//                contentScale = ContentScale.FillHeight
//            )

            WeatherDataColumn(
                hourlyChartData = hourlyChartData,
                dailyChartData = dailyChartData,
                pageIndex = pageNumber,
                appSettings = appSettings,
                detailState = detailState,
                chartState = chartState,
                dailyChartState = dailyChartState,
                scrollState = scrollState,
                showTemperature = showTemperature,
                spaceAboveTitle = spaceAboveTitle,
                bottomPadding = paddingValues.calculateBottomPadding(),
                headerHeight = headerHeight,
                cornerRadius = titleCornerRadius,
                timeAlpha = timeAlpha,
                onAddYAxis = { start, end, diagramHeight, textMeasurer, axisIndex: Int? ->
                    detailViewModel.addYAxis(
                        start,
                        end,
                        pageNumber,
                        diagramHeight,
                        textMeasurer,
                        axisIndex
                    )
                },
                onUpdateDailyYAxis = { quantity: DailyWeatherQuantity->
                    detailViewModel.updateDailyYAxis(quantity, pageNumber)
                },
                onMoveHourlyDiagramAxis = { offset ->
                    detailViewModel.moveHourlyDiagramAxis(
                        offset = offset,
                        pageIndex = pageNumber
                    )
                },
                onScaleHourlyDiagramAxis = { center, zoomX, zoomY ->
                    detailViewModel.scaleHourlyDiagramAxis(
                        center = center,
                        scaleX = zoomX,
                        scaleY = zoomY,
                        pageIndex = pageNumber
                    )
                },
                onScaleBack = { center ->
                    coroutineScope.launch {
                        detailViewModel.scaleToNormalHourlyDiagramAxis(center, pageNumber)
                    }
                },
                undoHourlyDiagramScaleMove = {
                    coroutineScope.launch {
                        detailViewModel.undoHourlyDiagramAxisMoveScale(pageNumber)
                    }
                },
                onMoveDailyDiagramAxis = { offset ->
                    detailViewModel.moveDailyDiagramAxis(
                        offset = offset,
                        pageIndex = pageNumber
                    )
                },
                onScaleDailyDiagramAxis = { center, zoomX, zoomY ->
                    detailViewModel.scaleDailyDiagramAxis(
                        center = center,
                        scaleX = zoomX,
                        scaleY = zoomY,
                        pageIndex = pageNumber
                    )
                },
                onScaleBackDaily = { center ->
                    coroutineScope.launch {
                        detailViewModel.scaleToNormalDailyDiagramAxis(center, pageNumber)
                    }
                },
                undoDailyDiagramScaleMove = {
                    coroutineScope.launch {
                        detailViewModel.undoDailyDiagramAxisMoveScale(pageNumber)
                    }
                },
                onCalculateHorizontalBarSeparation = detailViewModel::calculateHorizontalBarSeparation,
                onCalculateVerticalBarSeparation = detailViewModel::calculateVerticalBarSeparation,
                onAddChartQuantity = {
                    detailViewModel.addChartQuantity(it, pageNumber)
                },
                onRemoveChartQuantity = {
                    detailViewModel.removeChartQuantity(it, pageNumber)
                },
                onUpdateDailyChartQuantity = {
                    detailViewModel.updateDailyChartQuantity(it, pageNumber)
                },
//                onRemoveDailyChartQuantity = {
//                    detailViewModel.removeDailyChartQuantity(it, pageNumber)
//                },
                onUpdateHourlyDiagramSettingOpen = {
                    detailViewModel.updateHourlyDiagramSettingOpen(it, pageNumber)
                },
                onUpdateDailyDiagramSettingOpen = {
                    detailViewModel.updateDailyDiagramSettingOpen(it, pageNumber)
                },
                onUpdateChartWeatherConditionVisibility = detailViewModel::updateHourlyChartWeatherIconVisibility,
                onUpdateDailyChartWeatherConditionVisibility = detailViewModel::updateDailyChartWeatherIconVisibility,
                onUpdateChartDotsOnCurveVisibility = detailViewModel::updateHourlyChartDotsOnCurveVisibility,
                onUpdateChartCurveShadowVisibility = detailViewModel::updateHourlyChartCurveShadowVisibility,
                onUpdateChartSunRiseSetIconsVisibility = detailViewModel::updateHourlyChartSunRiseSetIconsVisibility,
                onUpdateChartGrids = detailViewModel::updateHourlyChartGrid,
                onUpdateChooseDiagramThemeDialogVisibility = {
                    detailViewModel.updateChooseDiagramThemeDialogVisibility(
                        visible = true,
                        pageIndex = pageNumber
                    )
                },
                onUpdateHourlyDiagramSettingRectangle = {
                    detailViewModel.updateHourlyDiagramSettingRectangle(it, pageNumber)
                },
                onUpdateDailyDiagramSettingRectangle = {
                    detailViewModel.updateDailyDiagramSettingRectangle(it, pageNumber)
                },
                onUpdateHourlyThumbPosition = {
                    detailViewModel.updateSliderThumbPosition(it, pageNumber)
                },
                onUpdateDailyThumbPosition = {
                    detailViewModel.updateDailySliderThumbPosition(it, pageNumber)
                },
                onUpdateHourlyCurveValueAtIndicator = { curveIndex, value ->
                    detailViewModel.updateCurveValueAtIndicator(curveIndex, value, pageNumber)
                },
                onUpdateDailyCurveValueAtIndicator = { valueMin, valueMax ->
                    detailViewModel.updateDailyCurveValueAtIndicator(valueMin, valueMax, pageNumber)
                },
                onNavigateToAQDetailScreen = {
                    navController.navigate(
                        route = "${Screens.AQDETAIL.name}/${pageNumber}/${detailViewModel.allLocations.size}"
                    )
                },
                sharedTransitionScope = sharedTransitionScope,
                animatedContentScope = animatedContentScope
            )

        }
    }

}

@Composable
fun Temperatures(
    modifier: Modifier = Modifier,
    detailState: DetailState,
    topSpacerHeight: Dp
) {
    val firstPointX = 0f
    val lastPointX = detailState.currentDayHourlyWeather.size.toFloat() - 1
    val (controlPoints1, controlPoints2) = quantityControlPoints(
        weatherData = detailState.currentDayHourlyWeather,
        airData = detailState.currentDayHourlyAirQuality,
        weatherQuantity = WeatherQuantity.TEMPERATURE,
    )
    val (apparentControlPoints1, apparentControlPoints2) = quantityControlPoints(
        weatherData = detailState.currentDayHourlyWeather,
        airData = detailState.currentDayHourlyAirQuality,
        weatherQuantity = WeatherQuantity.APPARENTTEMP,
    )
//    val controlPoints1 = detailState.currentDayHourlyWeather.map {
//        if (it.temperatureControl1X != null && it.temperatureControl1Y != null)
//            Offset(it.temperatureControl1X, it.temperatureControl1Y)
//        else null }
//    val controlPoints2 = detailState.currentDayHourlyWeather.map {
//        if (it.temperatureControl2X != null && it.temperatureControl2Y != null)
//            Offset(it.temperatureControl2X, it.temperatureControl2Y)
//        else null}

//    val apparentControlPoints1 = detailState.currentDayHourlyWeather.map {
//        if (it.apparentTemperatureControl1X != null && it.apparentTemperatureControl1Y != null)
//            Offset(it.apparentTemperatureControl1X, it.apparentTemperatureControl1Y)
//        else null }
//    val apparentControlPoints2 = detailState.currentDayHourlyWeather.map {
//        if (it.apparentTemperatureControl2X != null && it.apparentTemperatureControl2Y != null)
//            Offset(it.apparentTemperatureControl2X, it.apparentTemperatureControl2Y)
//        else null}

    val currentTemperature = cubicCurveXtoY(
        data = detailState.currentDayHourlyWeather.map { it.temperature },
        controlPoints1 = controlPoints1,
        controlPoints2 = controlPoints2,
        firstPointX = firstPointX,
        lastPointX = lastPointX,
        targetX = detailState.targetX
    )
    val apparentTemperature = cubicCurveXtoY(
        data = detailState.currentDayHourlyWeather.map { it.apparentTemperature },
        controlPoints1 = apparentControlPoints1,
        controlPoints2 = apparentControlPoints2,
        firstPointX = firstPointX,
        lastPointX = lastPointX,
        targetX = detailState.targetX
    )
    val maxTemperature = detailState.maxTemperature?.let { "%.1f".format(it) }
    val minTemperature = detailState.minTemperature?.let { "%.1f".format(it) }
    val currentTemperatureValue = currentTemperature?.let { "%.1f".format(it) }
    val apparentTemperatureValue = apparentTemperature?.let { "%.1f".format(it) }

    val extremeTempFontSize = 18.sp
    val mainTempFontSize = 105.sp
    val temperature = buildAnnotatedString {
        append(currentTemperatureValue ?: "")
        withStyle(
            style = SpanStyle(
                fontSize = mainTempFontSize / 2,
                baselineShift = BaselineShift.Superscript
            )
        ){
            append(WeatherQuantity.TEMPERATURE.unit(AppSettings()).trim('°'))
        }
    }

    Column(
        modifier = modifier
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Spacer(
            modifier = Modifier.height(topSpacerHeight)
        )
        Row {
            BorderedText(
                modifier = Modifier,
                text = temperature,
                fontFamily = Font(R.font.cmu_serif).toFontFamily(),
                fillColor = Color.White,
                strokeColor = Color.White,
                fontSize = mainTempFontSize,
                strokeWidth = 4f
            )
        }
        Row(
            modifier = Modifier
                .offset(y = (-16).dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(percent = 50)
                )
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(percent = 50)
                )
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ){
            BorderedText(
                text = AnnotatedString("${minTemperature ?: ""}°"),
                fontFamily = Font(R.font.cmu_serif).toFontFamily(),
                fontSize = extremeTempFontSize,
                fillColor = Color.Blue
            )
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(R.drawable.south_24),
                contentDescription = null,
                tint = Color.Blue
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Feels like: ${apparentTemperatureValue ?: ""}°",
                fontFamily = Font(R.font.cmu_serif).toFontFamily(),
                fontSize = extremeTempFontSize
            )
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                modifier = Modifier
                    .scale(scaleX = 1f, scaleY = -1f)
                    .size(24.dp),
                painter = painterResource(R.drawable.south_24),
                contentDescription = null,
                tint = Color.Red
            )
            BorderedText(
                text = AnnotatedString("${maxTemperature ?: ""}°"),
                fontFamily = Font(R.font.cmu_serif).toFontFamily(),
                fontSize = extremeTempFontSize,
                fillColor = Color.Red
            )
        }
    }
}

@Composable
fun BorderedText(
    modifier: Modifier = Modifier,
    text: AnnotatedString,
    fontFamily: FontFamily = Font(R.font.cmu_serif).toFontFamily(),
    strokeColor: Color? = null,
    fillColor: Color,
    fontSize: TextUnit = TextUnit.Unspecified,
    strokeWidth: Float = 2f,
    alignment: Alignment = Alignment.CenterStart
) {
    Box(modifier = modifier) {
        Text(
            modifier = Modifier.align(alignment),
            text = text,
            fontFamily = fontFamily,
            color = strokeColor ?: fillColor,
            fontSize = fontSize,
            lineHeight = fontSize * 1.5f,
            style = TextStyle(drawStyle = Stroke(width = strokeWidth)),
        )
        Text(
            modifier = Modifier.align(alignment),
            text = text,
            fontFamily = fontFamily,
            color = fillColor,
            fontSize = fontSize,
            lineHeight = fontSize * 1.5f,
        )
    }
}

fun Float.toDp(density: Density) = with(density){
    this@toDp.toDp()
}


@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true)
@Composable
fun DetailPagerScreenPreview() {
    WeathererTheme{
        SharedTransitionLayout {
            AnimatedContent<Boolean>(
                targetState = true, label = ""
            ) {targetState ->
                DetailPagerScreen(
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this
                )
                targetState
            }
        }
    }
}