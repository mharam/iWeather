package com.takaapoo.weatherer.ui.screens.detail

import android.graphics.Bitmap
import androidx.compose.animation.ExperimentalSharedTransitionApi
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
import androidx.compose.foundation.layout.navigationBars
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
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.core.graphics.createBitmap
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.transformations
import coil3.size.Size
import coil3.toBitmap
import coil3.transform.Transformation
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.takaapoo.weatherer.MainActivity
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.data.local.LocalDailyWeather
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.DailyChartState
import com.takaapoo.weatherer.domain.model.DetailState
import com.takaapoo.weatherer.domain.model.HistoricDailyChartDto
import com.takaapoo.weatherer.domain.model.HistoricHourlyChartDto
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import com.takaapoo.weatherer.domain.model.HourlyChartState
import com.takaapoo.weatherer.ui.screens.detail.daily_diagram.DailyDiagramSettingsOptions
import com.takaapoo.weatherer.ui.screens.detail.historical.HistoricHourlyDiagramSettingsOptions
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.HourlyDiagramSettingsOptions
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.weatherCurveXtoY
import com.takaapoo.weatherer.ui.theme.Cmu
import com.takaapoo.weatherer.ui.theme.Transparent
import com.takaapoo.weatherer.ui.theme.WeathererTheme
import com.takaapoo.weatherer.ui.theme.customColorScheme
import com.takaapoo.weatherer.ui.theme.gerdFontFamily
import com.takaapoo.weatherer.ui.viewModels.DetailViewModel
import com.takaapoo.weatherer.utility.BorderedText
import com.takaapoo.weatherer.utility.toDp
import com.takaapoo.weatherer.utility.toPx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

var detailScreenWidth = 0
const val bitmapWidth = 1248
const val bitmapHeight = 832

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailPagerScreen(
    modifier: Modifier = Modifier,
    detailState: DetailState,
    detailViewModel: DetailViewModel,
    hourlyChartState: State<HourlyChartState> = remember { mutableStateOf(HourlyChartState()) },
    dailyChartState: DailyChartState = DailyChartState(),
    historicalChartState: HourlyChartState = HourlyChartState(),
    historicalDailyChartState: DailyChartState = DailyChartState(),
    hourlyChartData: List<HourlyChartDto> = emptyList(),
    dailyChartData: List<LocalDailyWeather> = emptyList(),
    historicalHourlyData: List<HistoricHourlyChartDto> = emptyList(),
    historicalDailyData: List<HistoricDailyChartDto> = emptyList(),
    pageNumber: Int = 0,
    singlePane: Boolean = true,
    appSettings: AppSettings = AppSettings(),
    initialLandscape: Bitmap,
    trialPeriodExpired: Boolean?,

//    dataReady: Boolean = true,
//    sharedTransitionScope: SharedTransitionScope,
//    animatedContentScope: AnimatedContentScope,
    onResetScrollStates: suspend CoroutineScope.() -> Unit,
    onNavigateUp: (Int) -> Unit,
    onNavigateToAQDetail: (Int, Int) -> Unit,
    onNavigateToDonateScreen: () -> Unit,
    onNavigateToLoginScreen: () -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.getTop(density)
    val spaceAboveTitle = 1f * (context as MainActivity).windowHeight
    val titleInitialScroll = 0.3f * spaceAboveTitle
    val headerHeight = (64.dp).toPx(density) + statusBarHeight
    val temperatureTopSpacerHeight = (0.5f * context.windowHeight).toDp(density)

//    val locationsFlow by detailViewModel.allLocationsFlow.collectAsStateWithPermission(dataReady)
//    val location = locationsFlow.getOrElse(
//        index = pageNumber,
//        defaultValue = { Location() }
//    )
    val coroutineScope = rememberCoroutineScope()
    val updatingPageNumber by rememberUpdatedState(newValue = pageNumber)

    val scrollState = detailState.scrollState

    val titleCornerRadiusProvider = {
        ((spaceAboveTitle - scrollState.value)/headerHeight).coerceIn(0f,1f) * 40.dp
    }
//    val showTemperature by remember(pageNumber) {
//        derivedStateOf { scrollState.value < context.windowHeight }
//    }

    var imageBitmap by remember { mutableStateOf(initialLandscape) }
    LaunchedEffect(
        key1 = detailState.lastPicId,
        key2 = detailState.picName,
        key3 = detailState.currentPictureUrl
    ) {
//        Log.i("land1", "lastPicId = ${detailState.lastPicId} , picName = ${detailState.picName} , url = ${detailState.currentPictureUrl}")
        val file = File(
            context.filesDir,
            "pictures/pic${detailState.lastPicId}/${detailState.picName}.jpeg"
        )
        val request = ImageRequest.Builder(context)
            .data(
                if (file.exists()) file
                else "${detailState.currentPictureUrl}/${detailState.picName}.jpeg"
            )
            .transformations(object : Transformation() {
                override val cacheKey: String = "force_alpha"
                override suspend fun transform(input: coil3.Bitmap, size: Size): coil3.Bitmap {
                    return input.copy(Bitmap.Config.ARGB_8888, true)
                }
            })
            .target {
                imageBitmap = it.toBitmap()
            }
            .build()

        context.imageLoader.execute(request)
    }

//    val blurredImage by remember(key1 = imageBitmap) {
//        mutableStateOf(Toolkit.blur(inputBitmap = imageBitmap, radius = 3).asImageBitmap())
//    }
    var infoBottomSheetVisible by rememberSaveable { mutableStateOf(false) }
    val infoSheetState = rememberModalBottomSheetState()

    LaunchedEffect(scrollState.isScrollInProgress){
        if (!scrollState.isScrollInProgress &&
            scrollState.value.toFloat() in spaceAboveTitle - statusBarHeight .. spaceAboveTitle){
            scrollState.animateScrollTo(
                value = spaceAboveTitle.toInt() + 1,
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
            }
        }
    }
    LaunchedEffect(key1 = detailState.navigateBack) {
        if (detailState.navigateBack){
            launch {
                onResetScrollStates()
            }
            scrollState.animateScrollTo(
                value = 0,
                animationSpec = tween(durationMillis = 500)
            )
            detailViewModel.updateNavigateBack(state = false, pageIndex = pageNumber)
            onNavigateUp(pageNumber)
        }
    }

    var topTabHeight = 0
    val layoutDirection = LocalLayoutDirection.current
    val scrollBehavior = customScrollBehavior(
        contentScrollState = scrollState,
        state = detailState.topBarState,
        topBarScrollLimit = spaceAboveTitle,
        canScroll = detailState::scrollEnabled
    )
    val primaryColor = MaterialTheme.colorScheme.primaryContainer

    Scaffold(
        modifier = modifier,
        topBar = {
            Column(
                modifier = Modifier
                    .graphicsLayer {
                        colorFilter = ColorFilter.tint(
                            color = if (scrollState.value < context.windowHeight) Transparent
                            else primaryColor,
                            blendMode = BlendMode.DstOver
                        )
                    }
//                    .background(
//                    /*if (showTemperature) Transparent else */MaterialTheme.colorScheme.primaryContainer
//                )
                ,
            ){
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            modifier = Modifier
                                .graphicsLayer {
                                    alpha = if (scrollState.value < context.windowHeight) 0f else 1f
                                },
                            text = detailState.locationName,
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
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    },
                    actions = {
                        when(detailState.selectedTab) {
                            DetailTabs.Now -> {}
                            DetailTabs.Hourly -> {
                                HourlyDiagramSettingsOptions(
                                    hourlyChartState = hourlyChartState.value,
                                    settingsOpen = detailState.hourlyDiagramSettingOpen,
                                    pageNumber = pageNumber,
                                    onUpdateHourlyDiagramSettingOpen = remember(pageNumber) {
                                        detailViewModel::updateHourlyDiagramSettingOpen
                                    },
                                    onUpdateChartWeatherConditionVisibility = remember(pageNumber) {
                                        detailViewModel::updateHourlyChartWeatherIconVisibility
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
                                    onUpdateChartTheme = remember(pageNumber) {
                                        detailViewModel::updateHourlyChartTheme
                                    }
                                )
                            }
                            DetailTabs.Daily -> {
                                DailyDiagramSettingsOptions(
                                    hourlyChartState = hourlyChartState.value,
                                    settingsOpen = detailState.dailyDiagramSettingOpen,
                                    pageNumber = pageNumber,
                                    weatherConditionIconsVisible = dailyChartState.weatherConditionIconsVisible,
                                    onUpdateDailyDiagramSettingOpen = remember(pageNumber) {
                                        detailViewModel::updateDailyDiagramSettingOpen
                                    },
                                    onUpdateChartWeatherConditionVisibility = remember(pageNumber) {
                                        detailViewModel::updateDailyChartWeatherIconVisibility
                                    },
                                    onUpdateChartCurveShadowVisibility = remember(pageNumber) {
                                        detailViewModel::updateHourlyChartCurveShadowVisibility
                                    },
                                    onUpdateChartGrids = remember(pageNumber) {
                                        detailViewModel::updateHourlyChartGrid
                                    },
                                    onUpdateChartTheme = remember(pageNumber) {
                                        detailViewModel::updateHourlyChartTheme
                                    }
                                )
                            }
                            DetailTabs.History -> {
                                if (appSettings.hasDonated) {
                                    HistoricHourlyDiagramSettingsOptions(
                                        hourlyChartState = hourlyChartState.value,
                                        settingsOpen = detailState.hourlyDiagramSettingOpen,
                                        pageNumber = pageNumber,
                                        onUpdateHourlyDiagramSettingOpen = remember(pageNumber) {
                                            detailViewModel::updateHourlyDiagramSettingOpen
                                        },
                                        onUpdateChartWeatherConditionVisibility = remember(
                                            pageNumber
                                        ) {
                                            detailViewModel::updateHourlyChartWeatherIconVisibility
                                        },
                                        onUpdateChartDotsOnCurveVisibility = remember(pageNumber) {
                                            detailViewModel::updateHourlyChartDotsOnCurveVisibility
                                        },
                                        onUpdateChartCurveShadowVisibility = remember(pageNumber) {
                                            detailViewModel::updateHourlyChartCurveShadowVisibility
                                        },
                                        onUpdateChartGrids = remember(pageNumber) {
                                            detailViewModel::updateHourlyChartGrid
                                        },
                                        onUpdateChartTheme = remember(pageNumber) {
                                            detailViewModel::updateHourlyChartTheme
                                        }
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        infoBottomSheetVisible = !infoBottomSheetVisible
                                    }
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.info_24px),
                                        contentDescription = "information"
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Transparent,
                        scrolledContainerColor = Transparent
                    ),
                    windowInsets = WindowInsets(
                        left = 0, top = TopAppBarDefaults.windowInsets.getTop(density), right = 0, bottom = 0
                    ),
                    scrollBehavior = scrollBehavior
                )
                topTabHeight = detailPagerTopTabs(
                    modifier = Modifier.graphicsLayer {
                        alpha = if (scrollState.value < context.windowHeight) 0f else 1f
                    },
                    localDateTime = detailState.localDateTime,
                    selectedTabIndex = detailState.selectedTab.ordinal,
                    onUpdateSelectedTabIndex = {
                        detailViewModel.updateSelectedTabIndex(tab = it, pageIndex = pageNumber)
                    }
                )
            }
        }
    ) { paddingValues ->
        /*AnimatedVisibility (detailState.chooseDiagramThemeDialogVisible) {
            ChooseDiagramThemeDialog(
                onConfirmation = {
                    detailViewModel.updateChooseDiagramThemeDialogVisibility(
                        visible = false,
                        pageIndex = pageNumber
                    )
                },
                chartTheme = hourlyChartState.value.chartTheme,
                onSelectTheme = detailViewModel::updateHourlyChartTheme
            )
        }*/

        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned {
                    detailScreenWidth = it.size.width
                }
        ) {
            val backdrop = rememberLayerBackdrop()
            Image(
                modifier = Modifier.fillMaxSize()
                    .layerBackdrop(backdrop),
                bitmap = imageBitmap.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.FillHeight
            )
            Temperatures(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        val temperatureAlpha = (scrollState.value.toFloat() / titleInitialScroll)
                            .coerceAtMost(1f)
                        val temperatureY = when {
                            scrollState.value < 1.2f * titleInitialScroll -> 0f
                            else -> -scrollState.value + 1.2f * titleInitialScroll
                        }
                        val scale = 0.8f + 0.2f * temperatureAlpha

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
//            if (showTemperature) {
                /*Image(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawWithContent {
                            val radius = titleCornerRadiusProvider().toPx()
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
                                            cornerRadius = CornerRadius(radius, radius)
                                        )
                                    )
                                }
                            ) {
                                this@drawWithContent.drawContent()
                            }
                        },
                    bitmap = blurredImage,
                    contentDescription = null,
                    contentScale = ContentScale.FillHeight
                )*/
//            }

            WeatherDataColumn(
                modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                endPadding = paddingValues.calculateEndPadding(layoutDirection),
                hourlyChartData = hourlyChartData,
                dailyChartData = dailyChartData,
                historicalHourlyData = historicalHourlyData,
                historicalDailyData = historicalDailyData,
                appSettings = appSettings,
                detailState = detailState,
                hourlyChartState = hourlyChartState.value,
                dailyChartState = dailyChartState,
                historicalChartState = historicalChartState,
                historicalDailyChartState = historicalDailyChartState,
                spaceAboveTitle = spaceAboveTitle,
                bottomPadding = paddingValues.calculateBottomPadding(),
                headerHeight = headerHeight,
                cornerRadiusProvider = titleCornerRadiusProvider,
                tab = detailState.selectedTab,
                tabHeight = topTabHeight.toFloat(),
                initialXAxisRange = detailViewModel.initialXAxisStart..detailViewModel.initialXAxisEnd,
                historicInitialXAxisRange = detailViewModel.historicInitialXAxisStart..
                        detailViewModel.historicInitialXAxisEnd,
                backdrop = backdrop,
                trialPeriodExpired = trialPeriodExpired,
//                timeAlpha = timeAlpha,
                onAddYAxis = remember(pageNumber) {
                    {
                            start, end, diagramHeight, textMeasurer, axisIndex: Int?,
                            curveAnimatorInitialValue: Float, isHistoric,
                        ->
                        detailViewModel.addYAxis(
                            start = start,
                            end = end,
                            pageIndex = updatingPageNumber,
                            diagramHeight = diagramHeight,
                            textMeasurer = textMeasurer,
                            axisIndex = axisIndex,
                            curveAnimatorInitialValue = curveAnimatorInitialValue,
                            isHistoric = isHistoric
                        )
                    }
                },
                onUpdateDailyYAxis = remember(pageNumber) {
                    { quantity, curveAnimatorInitialValue, isHistoric ->
                        detailViewModel.updateDailyYAxis(
                            quantity, updatingPageNumber,
                            isHistoric = isHistoric,
                            curveAnimatorInitialValue
                        )
                    }
                },
                onMoveHourlyDiagramAxis = remember(pageNumber) {
                    { offset, isHistoric ->
                        detailViewModel.moveHourlyDiagramAxis(
                            offset = offset,
                            pageIndex = updatingPageNumber,
                            isHistoric = isHistoric
                        )
                    }
                },
                onScaleHourlyDiagramAxis = remember(pageNumber) {
                    { center, zoomX, zoomY, isHistoric ->
                        detailViewModel.scaleHourlyDiagramAxis(
                            center = center,
                            scaleX = zoomX,
                            scaleY = zoomY,
                            pageIndex = updatingPageNumber,
                            isHistoric = isHistoric
                        )
                    }
                },
                onScaleBack = remember(pageNumber) {
                    { center, isHistoric ->
                        coroutineScope.launch {
                            detailViewModel.scaleToNormalHourlyDiagramAxis(
                                center,
                                updatingPageNumber,
                                isHistoric
                            )
                        }
                    }
                },
                undoHourlyDiagramScaleMove = remember(pageNumber) {
                    {
                        coroutineScope.launch {
                            detailViewModel.undoHourlyDiagramAxisMoveScale(updatingPageNumber)
                        }
                    }
                },
                onMoveDailyDiagramAxis = remember(pageNumber) {
                    { offset, isHistoric ->
                        detailViewModel.moveDailyDiagramAxis(
                            offset = offset,
                            pageIndex = updatingPageNumber,
                            isHistoric = isHistoric
                        )
                    }
                },
                onScaleDailyDiagramAxis = remember(pageNumber) {
                    { center, zoomX, zoomY, isHistoric ->
                        detailViewModel.scaleDailyDiagramAxis(
                            center = center,
                            scaleX = zoomX,
                            scaleY = zoomY,
                            pageIndex = updatingPageNumber,
                            isHistoric = isHistoric
                        )
                    }
                },
                onScaleBackDaily = remember(pageNumber) {
                    { center, isHistoric ->
                        coroutineScope.launch {
                            detailViewModel.scaleToNormalDailyDiagramAxis(
                                center,
                                updatingPageNumber,
                                isHistoric
                            )
                        }
                    }
                },
                undoDailyDiagramScaleMove = remember(pageNumber) {
                    {
                        coroutineScope.launch {
                            detailViewModel.undoDailyDiagramAxisMoveScale(updatingPageNumber)
                        }
                    }
                },
                onCalculateHorizontalBarSeparation = remember(pageNumber) {
                    detailViewModel::calculateHorizontalBarSeparation
                },
                onCalculateVerticalBarSeparation = remember(pageNumber) {
                    detailViewModel::calculateVerticalBarSeparation
                },
                onAddHourlyChartQuantity = remember(pageNumber) {
                    { hourlyWeatherQuantity, isHistoric ->
                        detailViewModel.addChartQuantity(
                            hourlyWeatherQuantity,
                            updatingPageNumber,
                            isHistoric
                        )
                    }
                },
                onRemoveHourlyChartQuantity = remember(pageNumber) {
                    { hourlyWeatherQuantity, isHistoric ->
                        detailViewModel.removeChartQuantity(
                            hourlyWeatherQuantity,
                            updatingPageNumber,
                            isHistoric
                        )
                    }
                },
                onUpdateDailyChartQuantity = remember(pageNumber) {
                    { quantity, isHistoric ->
                        detailViewModel.updateDailyChartQuantity(
                            quantity,
                            updatingPageNumber,
                            isHistoric
                        )
                    }
                },
                onUpdateHourlyThumbPosition = remember(pageNumber) {
                    { movement, isHistoric ->
                        detailViewModel.setSliderThumbPosition(
                            movement,
                            updatingPageNumber,
                            isHistoric
                        )
                    }
                },
                onUpdateDailyThumbPosition = remember(pageNumber) {
                    { movement, isHistoric ->
                        detailViewModel.setDailySliderThumbPosition(
                            movement,
                            updatingPageNumber,
                            isHistoric
                        )
                    }
                },
                onUpdateHourlyCurveValueAtIndicator = remember(pageNumber) {
                    { curveIndex, value, isHistoric ->
                        detailViewModel.updateCurveValueAtIndicator(
                            curveIndex,
                            value,
                            updatingPageNumber,
                            isHistoric
                        )
                    }
                },
//                onUpdateDailyCurveValueAtIndicator = remember(pageNumber) {
//                    { valueMin, valueMax, isHistoric ->
//                        detailViewModel.updateDailyCurveValueAtIndicator(
//                            valueMin,
//                            valueMax,
//                            updatingPageNumber,
//                            isHistoric
//                        )
//                    }
//                },
                onNavigateToAQDetailScreen = remember(pageNumber) {
                    {
                        onNavigateToAQDetail(updatingPageNumber, scrollState.value)
                    }
                },
                onUpdateSelectedTab = remember(pageNumber) {
                    { tab ->
                        detailViewModel.updateSelectedTabIndex(
                            tab = tab,
                            pageIndex = updatingPageNumber
                        )
                    }
                },
                onAnimateHourlyDiagramAxis = remember(pageNumber) {
                    { date ->
                        coroutineScope.launch {
                            detailViewModel.animateHourlyDiagramAxis(
                                date = date,
                                pageIndex = updatingPageNumber
                            )
                        }
                    }
                },
                onReloadHistoricalData = remember(pageNumber) {
                    { detailViewModel.reloadHistoricalData(updatingPageNumber) }
                },
                onAnimateDailyDiagramAxis = remember(pageNumber) {
                    { date ->
                        coroutineScope.launch {
                            detailViewModel.animateDailyDiagramAxis(
                                date = date,
                                pageIndex = updatingPageNumber
                            )
                        }
                    }
                },
                onReloadHistoricalDailyData = remember(pageNumber) {
                    { detailViewModel.reloadHistoricalDailyData(updatingPageNumber) }
                },
                onNavigateToDonateScreen = onNavigateToDonateScreen,
                onNavigateToLoginScreen = onNavigateToLoginScreen
//                sharedTransitionScope = sharedTransitionScope,
//                animatedContentScope = animatedContentScope
            )

        }
        if (infoBottomSheetVisible) {
            ModalBottomSheet(
                sheetState = infoSheetState,
                onDismissRequest = {
                    infoBottomSheetVisible = false
                },
                contentWindowInsets = {
                    WindowInsets.navigationBars
                }
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = "Historical Weather",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = gerdFontFamily
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = stringResource(R.string.history_weather_description),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }

}

@Composable
fun Temperatures(
    modifier: Modifier = Modifier,
    detailState: DetailState,
    appSettings: AppSettings,
    topSpacerHeight: Dp,
) {
    val firstPointX = 0f
    val lastPointX = detailState.currentDayHourlyWeather.size.toFloat() - 1
    val currentTemperature = weatherCurveXtoY(
        quantity = HourlyWeatherQuantity.TEMPERATURE,
        appSettings = appSettings,
        hourlyWeather = detailState.currentDayHourlyWeather,
        firstPointX = firstPointX,
        lastPointX = lastPointX,
        targetX = detailState.targetX
    )
    val apparentTemperature = weatherCurveXtoY(
        quantity = HourlyWeatherQuantity.APPARENTTEMP,
        appSettings = appSettings,
        hourlyWeather = detailState.currentDayHourlyWeather,
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
            append(HourlyWeatherQuantity.TEMPERATURE.unit(appSettings).trim('°'))
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
                fontFamily = Cmu,
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
                    color = MaterialTheme.colorScheme.secondaryContainer,
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
                fontFamily = Cmu,
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
                fontFamily = Cmu,
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
                fontFamily = Cmu,
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
                    detailViewModel = hiltViewModel<DetailViewModel>(
                        viewModelStoreOwner = rememberViewModelStoreOwner()
                    ),
//                    scrollState = rememberScrollState(),
                    initialLandscape = createBitmap(bitmapWidth, bitmapHeight),
                    trialPeriodExpired = null,
                    onResetScrollStates = {},
                    onNavigateUp = { _ -> },
                    onNavigateToAQDetail = { _, _ -> },
                    onNavigateToDonateScreen = {},
                    onNavigateToLoginScreen = {}
//                    sharedTransitionScope = this@SharedTransitionLayout,
//                    animatedContentScope = this
                )
//                targetState
//            }
//        }
    }
}