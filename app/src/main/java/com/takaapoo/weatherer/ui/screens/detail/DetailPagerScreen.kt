package com.takaapoo.weatherer.ui.screens.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
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
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.toFontFamily
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.renderscript.Toolkit
import com.takaapoo.weatherer.MainActivity
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.data.local.LocalDailyWeather
import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.DailyChartState
import com.takaapoo.weatherer.domain.model.DetailState
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import com.takaapoo.weatherer.domain.model.HourlyChartState
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.cubicCurveXtoY
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.linearCurveXtoY
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.quantityControlPoints
import com.takaapoo.weatherer.ui.theme.Transparent
import com.takaapoo.weatherer.ui.theme.WeathererTheme
import com.takaapoo.weatherer.ui.theme.customColorScheme
import com.takaapoo.weatherer.ui.utility.BorderedText
import com.takaapoo.weatherer.ui.utility.toDp
import com.takaapoo.weatherer.ui.utility.toPx
import com.takaapoo.weatherer.ui.viewModels.DetailViewModel
import com.takaapoo.weatherer.ui.viewModels.timeFontFamily
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

var detailScreenWidth = 0
//const val LAST_PAGE_NUMBER = "last_page_number"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailPagerScreen(
    modifier: Modifier = Modifier,
    detailState: DetailState,
    detailViewModel: DetailViewModel,
    hourlyChartState: HourlyChartState = HourlyChartState(),
    dailyChartState: DailyChartState = DailyChartState(),
    hourlyChartData: List<HourlyChartDto> = emptyList(),
    dailyChartData: List<LocalDailyWeather> = emptyList(),
    pageNumber: Int = 0,
    singlePane: Boolean = true,
    appSettings: AppSettings = AppSettings(),
//    sharedTransitionScope: SharedTransitionScope,
//    animatedContentScope: AnimatedContentScope,
    onResetScrollStates: suspend CoroutineScope.() -> Unit,
    onNavigateUp: (pageNumber: Int) -> Unit,
    onNavigateToAQDetail: (pageNumber: Int, scrollValue: Int) -> Unit
) {
//    Log.i("comp1", "DetailPagerScreen")
    val context = LocalContext.current
    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.getTop(density)
    val spaceAboveTitle = 1f * (context as MainActivity).windowHeight
    val titleInitialScroll = 0.3f * spaceAboveTitle
    val headerHeight = (64.dp).toPx(density) + statusBarHeight
    val temperatureTopSpacerHeight = (0.5f * context.windowHeight).toDp(density)

    val locationsFlow by detailViewModel.allLocationsFlow.collectAsStateWithLifecycle()
    val location = locationsFlow.getOrElse(
        index = pageNumber,
        defaultValue = { Location() }
    )
    val coroutineScope = rememberCoroutineScope()
    val updatingPageNumber by rememberUpdatedState(newValue = pageNumber)

    val scrollState = detailState.scrollState

    val titleCornerRadius = ((spaceAboveTitle - scrollState.value)/headerHeight).coerceIn(0f,1f) * 32.dp
    val temperatureY = when {
//        scrollState.value < titleInitialScroll -> (scrollState.value - titleInitialScroll) / 5
        scrollState.value < 1.2f * titleInitialScroll -> 0f
        else -> -scrollState.value + 1.2f * titleInitialScroll
    }
    val temperatureAlpha = (scrollState.value.toFloat() / titleInitialScroll).coerceAtMost(1f)
    val showTemperature by remember(pageNumber) {
        derivedStateOf { scrollState.value < context.windowHeight }
    }

    val imageBitmap by remember(key1 = pageNumber) {
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
    val blurredImage by remember(key1 = pageNumber) {
        mutableStateOf(Toolkit.blur(inputBitmap = imageBitmap, radius = 3).asImageBitmap())
//        mutableStateOf(ContextCompat.getDrawable(context, R.drawable.ax4_c)!!.toBitmap().asImageBitmap())
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
    LaunchedEffect(key1 = pageNumber){
        delay(100)
        launch {
            if (scrollState.value == 0) {
                scrollState.animateScrollTo(
                    value = titleInitialScroll.toInt(),
                    animationSpec = tween(durationMillis = 500)
                )
                delay(100)
                detailViewModel.initializeXAxisBounds(updatingPageNumber)
                detailViewModel.updateChartsVisibility(visible = true, pageIndex = updatingPageNumber)
            }
        }
    }
    LaunchedEffect(key1 = detailState.navigateBack) {
        if (detailState.navigateBack){
            launch{
                onResetScrollStates()
            }
            scrollState.animateScrollTo(
                value = 0,
                animationSpec = tween(durationMillis = 500)
            )
            detailViewModel.updateNavigateBack(state = false, pageIndex = pageNumber)
            detailViewModel.updateChartsVisibility(visible = false, pageIndex = pageNumber)
            onNavigateUp(pageNumber)
        }
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
    val timeAlpha by remember(key1 = pageNumber) {
        derivedStateOf {
            when {
                scrollState.value < spaceAboveTitle -> 1f
                else -> ((spaceAboveTitle + subtitleVerticalSpace - scrollState.value)/subtitleVerticalSpace)
                    .coerceAtLeast(0f)
            }
        }
    }

    val layoutDirection = LocalLayoutDirection.current
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
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                },
                navigationIcon = {
                    if (singlePane) {
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
        AnimatedVisibility (detailState.chooseDiagramThemeDialogVisible) {
            ChooseDiagramThemeDialog(
                onConfirmation = {
                    detailViewModel.updateChooseDiagramThemeDialogVisibility(
                        visible = false,
                        pageIndex = pageNumber
                    )
                },
                chartTheme = hourlyChartState.chartTheme,
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
                Image(
                    modifier = Modifier.fillMaxSize(),
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
                            TransformOrigin(0.5f, 0.5f)
                        },
                    detailState = detailState,
                    appSettings = appSettings,
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
                        /*.graphicsLayer {
                            scaleX = axScale
                            scaleY = axScale
                        }*/,
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
                endPadding = paddingValues.calculateEndPadding(layoutDirection),
                hourlyChartData = hourlyChartData,
                dailyChartData = dailyChartData,
//                pageIndex = pageNumber,
                appSettings = appSettings,
                detailState = detailState,
                hourlyChartState = hourlyChartState,
                dailyChartState = dailyChartState,
//                scrollState = scrollState,
//                showTemperature = showTemperature,
                spaceAboveTitle = spaceAboveTitle,
                bottomPadding = paddingValues.calculateBottomPadding(),
                headerHeight = headerHeight,
                cornerRadius = titleCornerRadius,
                timeAlpha = timeAlpha,
                onAddYAxis = remember(pageNumber) {{ start, end, diagramHeight, textMeasurer,
                                                     axisIndex: Int?, curveAnimatorInitialValue: Float ->
                    detailViewModel.addYAxis(
                        start,
                        end,
                        updatingPageNumber,
                        diagramHeight,
                        textMeasurer,
                        axisIndex,
                        curveAnimatorInitialValue
                    )
                }},
                onUpdateDailyYAxis = remember(pageNumber) {{ quantity: DailyWeatherQuantity->
                    detailViewModel.updateDailyYAxis(quantity, updatingPageNumber)
                }},
                onMoveHourlyDiagramAxis = remember(pageNumber) {{ offset ->
                    detailViewModel.moveHourlyDiagramAxis(
                        offset = offset,
                        pageIndex = updatingPageNumber
                    )
                }},
                onScaleHourlyDiagramAxis = remember(pageNumber) {{ center, zoomX, zoomY ->
                    detailViewModel.scaleHourlyDiagramAxis(
                        center = center,
                        scaleX = zoomX,
                        scaleY = zoomY,
                        pageIndex = updatingPageNumber
                    )
                }},
                onScaleBack = remember(pageNumber) {{ center ->
                    coroutineScope.launch {
                        detailViewModel.scaleToNormalHourlyDiagramAxis(center, updatingPageNumber)
                    }
                }},
                undoHourlyDiagramScaleMove = remember(pageNumber) {{
                    coroutineScope.launch {
                        detailViewModel.undoHourlyDiagramAxisMoveScale(updatingPageNumber)
                    }
                }},
                onMoveDailyDiagramAxis = remember(pageNumber) {{ offset ->
                    detailViewModel.moveDailyDiagramAxis(
                        offset = offset,
                        pageIndex = updatingPageNumber
                    )
                }},
                onScaleDailyDiagramAxis = remember(pageNumber) {{ center, zoomX, zoomY ->
                    detailViewModel.scaleDailyDiagramAxis(
                        center = center,
                        scaleX = zoomX,
                        scaleY = zoomY,
                        pageIndex = updatingPageNumber
                    )
                }},
                onScaleBackDaily = remember(pageNumber) {{ center ->
                    coroutineScope.launch {
                        detailViewModel.scaleToNormalDailyDiagramAxis(center, updatingPageNumber)
                    }
                }},
                undoDailyDiagramScaleMove = remember(pageNumber){{
                    coroutineScope.launch {
                        detailViewModel.undoDailyDiagramAxisMoveScale(updatingPageNumber)
                    }
                }},
                onCalculateHorizontalBarSeparation = remember(pageNumber) {
                    detailViewModel::calculateHorizontalBarSeparation
                },
                onCalculateVerticalBarSeparation = remember(pageNumber) {
                    detailViewModel::calculateVerticalBarSeparation
                },
                onAddChartQuantity = remember(pageNumber) {{
                    detailViewModel.addChartQuantity(it, updatingPageNumber)
                }},
                onRemoveChartQuantity = remember(pageNumber) {{
                     detailViewModel.removeChartQuantity(it, updatingPageNumber)
                }},
                onUpdateDailyChartQuantity = remember(pageNumber) {{
                    detailViewModel.updateDailyChartQuantity(it, updatingPageNumber)
                }},
//                onRemoveDailyChartQuantity = {
//                    detailViewModel.removeDailyChartQuantity(it, pageNumber)
//                },
                onUpdateHourlyDiagramSettingOpen = remember(pageNumber) {{
                    detailViewModel.updateHourlyDiagramSettingOpen(it, updatingPageNumber)
                }},
                onUpdateDailyDiagramSettingOpen = remember(pageNumber) {{
                    detailViewModel.updateDailyDiagramSettingOpen(it, updatingPageNumber)
                }},
                onUpdateChartWeatherConditionVisibility = remember(pageNumber) {
                    detailViewModel::updateHourlyChartWeatherIconVisibility
                },
                onUpdateDailyChartWeatherConditionVisibility = remember(pageNumber) {
                    detailViewModel::updateDailyChartWeatherIconVisibility
                },
                onUpdateChartDotsOnCurveVisibility = remember(pageNumber) {
                    detailViewModel::updateHourlyChartDotsOnCurveVisibility
                },
                onUpdateChartCurveShadowVisibility = remember(pageNumber) {
                    detailViewModel::updateHourlyChartCurveShadowVisibility
                },
                onUpdateChartSunRiseSetIconsVisibility = remember(pageNumber) {
                    detailViewModel::updateHourlyChartSunRiseSetIconsVisibility
                },
                onUpdateChartGrids = remember(pageNumber) {
                    detailViewModel::updateHourlyChartGrid
                },
                onUpdateChooseDiagramThemeDialogVisibility = remember(pageNumber) {{
                    detailViewModel.updateChooseDiagramThemeDialogVisibility(
                        visible = true,
                        pageIndex = updatingPageNumber
                    )
                }},
                onUpdateHourlyDiagramSettingRectangle = remember(pageNumber) {{
                    detailViewModel.updateHourlyDiagramSettingRectangle(it, updatingPageNumber)
                }},
                onUpdateDailyDiagramSettingRectangle = remember(pageNumber) {{
                    detailViewModel.updateDailyDiagramSettingRectangle(it, updatingPageNumber)
                }},
                onUpdateHourlyThumbPosition = remember(pageNumber) {{
                    detailViewModel.updateSliderThumbPosition(it, updatingPageNumber)
                }},
                onUpdateDailyThumbPosition = remember(pageNumber) {{
                    detailViewModel.updateDailySliderThumbPosition(it, updatingPageNumber)
                }},
                onUpdateHourlyCurveValueAtIndicator = remember(pageNumber) {{ curveIndex, value ->
                    detailViewModel.updateCurveValueAtIndicator(curveIndex, value, updatingPageNumber)
                }},
                onUpdateDailyCurveValueAtIndicator = remember(pageNumber) {{ valueMin, valueMax ->
                    detailViewModel.updateDailyCurveValueAtIndicator(valueMin, valueMax, updatingPageNumber)
                }},
                onNavigateToAQDetailScreen = remember(pageNumber) {{
                    onNavigateToAQDetail(updatingPageNumber, scrollState.value)
                }},
//                sharedTransitionScope = sharedTransitionScope,
//                animatedContentScope = animatedContentScope
            )

        }
    }

}

@Composable
fun Temperatures(
    modifier: Modifier = Modifier,
    detailState: DetailState,
    appSettings: AppSettings,
    topSpacerHeight: Dp
) {
    val tempUnit = appSettings.temperatureUnit
    val firstPointX = 0f
    val lastPointX = detailState.currentDayHourlyWeather.size.toFloat() - 1
//    val (controlPoints1, controlPoints2) = quantityControlPoints(
//        weatherData = detailState.currentDayHourlyWeather,
//        airData = detailState.currentDayHourlyAirQuality,
//        weatherQuantity = WeatherQuantity.TEMPERATURE,
//    )
    val (apparentControlPoints1, apparentControlPoints2) = quantityControlPoints(
        weatherData = detailState.currentDayHourlyWeather,
        airData = detailState.currentDayHourlyAirQuality,
        weatherQuantity = WeatherQuantity.APPARENTTEMP,
    )
    val currentTemperature = linearCurveXtoY(
        data = detailState.currentDayHourlyWeather.map { it.temperature },
//        controlPoints1 = controlPoints1,
//        controlPoints2 = controlPoints2,
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
    val mainTempFontSize = 100.sp
    val temperature = buildAnnotatedString {
        append(currentTemperatureValue ?: "")
        withStyle(
            style = SpanStyle(
                fontSize = mainTempFontSize / 2,
                baselineShift = BaselineShift.Superscript
            )
        ){
            append(WeatherQuantity.TEMPERATURE.unit(appSettings).trim('°'))
        }
    }

    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ){
        Box {
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
                fillColor = MaterialTheme.customColorScheme.minTemperature
            )
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(R.drawable.south_24),
                contentDescription = null,
                tint = MaterialTheme.customColorScheme.minTemperature
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
        Spacer(
            modifier = Modifier.height(topSpacerHeight)
        )
    }
}


@OptIn(ExperimentalSharedTransitionApi::class)
@Preview(showBackground = true)
@Composable
fun DetailPagerScreenPreview() {
    WeathererTheme{
//        SharedTransitionLayout {
//            AnimatedContent<Boolean>(
//                targetState = true, label = ""
//            ) {targetState ->
                DetailPagerScreen(
                    detailState = DetailState(),
                    detailViewModel = hiltViewModel(),
//                    scrollState = rememberScrollState(),
                    onResetScrollStates = {},
                    onNavigateUp = {_ -> },
                    onNavigateToAQDetail = {_, _ -> }
//                    sharedTransitionScope = this@SharedTransitionLayout,
//                    animatedContentScope = this
                )
//                targetState
//            }
//        }
    }
}