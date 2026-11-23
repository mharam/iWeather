package com.takaapoo.weatherer.ui.screens.radar

import android.annotation.SuppressLint
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.animateValue
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.domain.MyResult
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.LocationsBriefState
import com.takaapoo.weatherer.domain.model.RadarState
import com.takaapoo.weatherer.domain.unit.Length
import com.takaapoo.weatherer.ui.screens.radar.RadarWeatherQuantity.Companion.applyUnit
import com.takaapoo.weatherer.ui.theme.Transparent
import com.takaapoo.weatherer.ui.theme.customColorScheme
import com.takaapoo.weatherer.ui.theme.isDarkTheme
import com.takaapoo.weatherer.utility.BorderedText
import com.takaapoo.weatherer.utility.clockGaugeWidth
import com.takaapoo.weatherer.utility.clockRotationCoefficient
import com.takaapoo.weatherer.utility.lerpMultipleColors
import com.takaapoo.weatherer.utility.mToUnit
import com.takaapoo.weatherer.utility.mostContrastColor
import com.takaapoo.weatherer.utility.roundToDecimalDigits
import com.takaapoo.weatherer.utility.toDp
import com.takaapoo.weatherer.utility.toPx
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.time.Clock
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.roundToLong
import kotlin.time.Duration.Companion.milliseconds

enum class TitleState { OneTitle, NewTitleEnter, OldTitleExit }

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun RadarScreen(
    modifier: Modifier = Modifier,
    radarRealState: State<RadarState>,
    locationState: List<LocationsBriefState>,
    appSettings: AppSettings,
    internetConnected: State<Boolean>,
    radarScreenWidth: Float,

    onUpdateSelectedQuantity: (RadarWeatherQuantity) -> Unit,
    onUpdateQuantityWeatherModel: (RadarWeatherQuantity, WeatherModels) -> Unit,
    onUpdateQuantityDialogVisible: (Boolean) -> Unit,
    onUpdateLoadingIndicatorVisible: (Boolean) -> Unit,
    onUpdateQuantityValue: (Float?) -> Unit,
    onUpdateElevation: (Float?) -> Unit,
    onUpdateSettingsOpen: (open: Boolean) -> Unit,
    onUpdateCrossHairVisibility: (visible: Boolean) -> Unit,
    onUpdateElevationVisibility: (visible: Boolean) -> Unit,
    onUpdateClockGaugeVisibility: (visible: Boolean) -> Unit,
    onUpdateRadarClock: (RadarClocks) -> Unit,
    onReloadRadarData: () -> Unit,

    onUpdateVisibleDayIndex: (newIndex: Int) -> Unit,
    onUpdateDayGaugeIndex: (newVisibleDay: Int) -> Unit,
    onUpdateClockGaugeRotation: (rotation: Float) -> Unit,
    onBalanceClockGaugeValues: () -> Unit,
    onResetDayGauge: () -> Unit,
    onInitializeClockGauge: () -> Unit,
    onUpdateClockGaugeTime: () -> Unit,
    onUpdateClockGaugeDragStopped: (Boolean) -> Unit,
    onUpdateNoDataWarningVisible: (visible: Boolean) -> Unit,
    onCalculateDataIndex: (weatherModel: WeatherModels) -> Int?,

    onNavigateUp: () -> Unit,
) {
    val density = LocalDensity.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val webView = remember { WebView(context) }
    val radarState = radarRealState.value

    var isPageReady by remember { mutableStateOf(false) }
    var selectedQuantity by rememberSaveable { mutableStateOf(radarState.quantity) }
    var selectedModel by rememberSaveable {
        mutableStateOf(radarState.quantityWeatherModels[selectedQuantity]!!)
    }

    val currentServerName by rememberUpdatedState(
        if (radarState.radarSpecs[selectedModel]?.variables?.contains(selectedQuantity.serverName) == true)
            selectedQuantity.serverName
        else
            selectedQuantity.serverName2
    )
    val currentRadarState by rememberUpdatedState(radarState)
    val currentWeatherModel by rememberUpdatedState(selectedModel)

    var titleAnimationState by rememberSaveable { mutableStateOf(TitleState.OneTitle) }
    var modelAnimationState by rememberSaveable { mutableStateOf(TitleState.OneTitle) }
    var quantityChangeSuccessful by rememberSaveable { mutableStateOf(true) }
    var modelChangeSuccessful by rememberSaveable { mutableStateOf(true) }

    val titleHeight = 48.dp
    val valueBarHeight = 24.dp

    val clockGaugeWidth by remember(key1 = radarScreenWidth) {
        mutableStateOf(clockGaugeWidth(radarScreenWidth, density))
    }
    val clockGaugeRotationCoefficient = clockRotationCoefficient(clockGaugeWidth.toPx(density))
    val itemHeight = dimensionResource(R.dimen.day_display_item_height).toPx(density)
    val dayListState = rememberLazyListState(
        initialFirstVisibleItemIndex = radarState.dayGaugeNaturalIndex,
        initialFirstVisibleItemScrollOffset = -(itemHeight/2f).toInt(),
    )

    val snackbarHostState = remember { SnackbarHostState() }

    onInitializeClockGauge()
    val isDark = MaterialTheme.isDarkTheme

    LaunchedEffect(Unit) {
        onUpdateElevation(null)
        onUpdateQuantityValue(null)
        titleAnimationState = TitleState.OneTitle
        modelAnimationState = TitleState.OneTitle
        quantityChangeSuccessful = true
        modelChangeSuccessful = true
    }
    LaunchedEffect(titleAnimationState) {
        if (titleAnimationState == TitleState.OldTitleExit) {
            delay(500.milliseconds)
            onUpdateSelectedQuantity(selectedQuantity)
            onUpdateQuantityWeatherModel(selectedQuantity, selectedModel)
            delay(500.milliseconds)
            titleAnimationState = TitleState.OneTitle
        }
    }
    LaunchedEffect(modelAnimationState) {
        if (modelAnimationState == TitleState.OldTitleExit) {
            delay(500.milliseconds)
            onUpdateQuantityWeatherModel(selectedQuantity, selectedModel)
            Log.i("update1", "selectedQuantity = $selectedQuantity , selectedModel = $selectedModel")
            delay(500.milliseconds)
            modelAnimationState = TitleState.OneTitle
        }
    }
    LaunchedEffect(radarState.elevationVisible, isPageReady) {
        if (isPageReady) {
            updateElevationVisible(
                webView = webView,
                visible = radarState.elevationVisible
            )
        }
    }
    LaunchedEffect(
        currentServerName,
        selectedModel.serverName,
        isPageReady,
    ) {
        if (isPageReady && (quantityChangeSuccessful || modelChangeSuccessful) && currentServerName != null) {
            onCalculateDataIndex(selectedModel)?.let { timeIndex ->
                if (timeIndex == -1) {
                    onUpdateQuantityValue(null)
                    titleAnimationState = TitleState.OldTitleExit
                    modelAnimationState = TitleState.OldTitleExit
                }
                onUpdateNoDataWarningVisible(timeIndex == -1)
                onUpdateLoadingIndicatorVisible(timeIndex != -1)
                updateWeatherLayer(
                    webView = webView,
                    variable = currentServerName!!,
                    weatherModel = selectedModel.serverName,
                    timeIndex = timeIndex,
                    dark = isDark
                )
            }
        }
    }
    LaunchedEffect(
        radarState.clockGaugeRotation.isRunning,
        dayListState.isScrollInProgress,
        radarState.clockGaugeNaturalRotation,
        radarState.dayGaugeNaturalIndex,
        isPageReady,
        currentServerName
    ) {
        if (!radarState.clockGaugeRotation.isRunning && !dayListState.isScrollInProgress &&
            radarState.clockGaugeDragStopped && isPageReady && currentServerName != null)
        {
            val clockGaugeTime = LocalDateTime.now(Clock.systemUTC())
                .truncatedTo(ChronoUnit.HOURS).plusHours(1)
                .plusDays(radarState.dayGaugeIndex.toLong())
                .plusHours((-radarState.clockGaugeRotation.value / 15).roundToLong())

            if (clockGaugeTime != radarState.clockGaugeTime) {
                onUpdateClockGaugeTime()
                val timeIndex = onCalculateDataIndex(selectedModel) ?: return@LaunchedEffect
                onUpdateNoDataWarningVisible(timeIndex == -1)
                onUpdateLoadingIndicatorVisible(timeIndex != -1)
                delay(2000.milliseconds)
                updateWeatherLayer(
                    webView = webView,
                    variable = currentServerName!!,
                    weatherModel = selectedModel.serverName,
                    timeIndex = timeIndex,
                    dark = isDark
                )
            }
        }
    }
    LaunchedEffect(internetConnected.value) {
        if (internetConnected.value && !radarState.clockGaugeRotation.isRunning &&
            !dayListState.isScrollInProgress && radarState.clockGaugeDragStopped &&
            isPageReady && currentServerName != null)
        {
            onCalculateDataIndex(selectedModel)?.let { timeIndex ->
                onUpdateNoDataWarningVisible(timeIndex == -1)
                onUpdateLoadingIndicatorVisible(timeIndex != -1)
                updateWeatherLayer(
                    webView = webView,
                    variable = currentServerName!!,
                    weatherModel = selectedModel.serverName,
                    timeIndex = timeIndex,
                    dark = isDark
                )
            }
        }
    }

    AnimatedVisibility(visible = radarState.quantityDialogVisible) {
        ChooseQuantityDialog(
            selectedQuantity = selectedQuantity,
            selectedModel = currentWeatherModel,
            quantityWeatherModels = radarState.quantityWeatherModels,
            radarSpecs = radarState.radarSpecs,

            onDismissRequest = { onUpdateQuantityDialogVisible(false) },
            onConfirmation = { quantity, model ->
                if (selectedQuantity == quantity && selectedModel == model) {
                    onCalculateDataIndex(model)?.let { timeIndex ->
                        if (timeIndex == -1) {
                            onUpdateQuantityValue(null)
                        }
                        onUpdateNoDataWarningVisible(timeIndex == -1)
                        onUpdateLoadingIndicatorVisible(timeIndex != -1)
                        updateWeatherLayer(
                            webView = webView,
                            variable = currentServerName!!,
                            weatherModel = model.serverName,
                            timeIndex = timeIndex,
                            dark = isDark
                        )
                    }
                } else {
                    if (selectedQuantity != quantity) {
                        quantityChangeSuccessful = true
                        selectedQuantity = quantity
                        titleAnimationState = TitleState.NewTitleEnter
                    }
                    if (selectedModel != model) {
                        modelChangeSuccessful = true
                        selectedModel = model
                        modelAnimationState = TitleState.NewTitleEnter
                    }
                }
            },
        )
    }

    when (radarState.radarInitializationStatus) {
        is MyResult.Error -> {
            RadarErrorScreen(
                modifier = Modifier.fillMaxSize(),
                onReloadRadarData = onReloadRadarData
            )
        }
        is MyResult.Loading -> RadarLoadingScreen(modifier = Modifier.fillMaxSize())
        is MyResult.Success<*> -> {
            Scaffold(
                modifier = modifier.fillMaxSize(),
                snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState) { data ->
                        Snackbar(
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.onBackground,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .padding(horizontal = 8.dp)
                                .offset(y = (-8).dp)
                                .border(
                                    width = dimensionResource(id = R.dimen.connector_line_width),
                                    color = MaterialTheme.colorScheme.error,
                                    shape = RoundedCornerShape(50)
                                )
                                .shadow(
                                    elevation = 2.dp,
                                    shape = RoundedCornerShape(50)
                                )
                        ) {
                            Row(
                                modifier = Modifier,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.error_24px),
                                    tint = MaterialTheme.colorScheme.error,
                                    contentDescription = null
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = data.visuals.message,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontFamily = FontFamily.Default
                                )
                            }
                        }
                    }
                },
                topBar = {
                    CenterAlignedTopAppBar(
                        modifier = Modifier.background(Transparent),
                        title = {
                            Title(
                                animationState = titleAnimationState,
                                modelAnimationState = modelAnimationState,
                                titleHeight = titleHeight,
                                quantity = radarRealState.value.quantity,
                                weatherModel = radarRealState.value.quantityWeatherModels[radarRealState.value.quantity]!!,
                                selectedQuantity = selectedQuantity,
                                selectedWeatherModel = currentWeatherModel,
                                loadingIndicatorVisible = radarRealState.value.loadingIndicatorVisible,
                                onUpdateQuantityDialogVisible = remember { onUpdateQuantityDialogVisible }
                            )
                        },
                        navigationIcon = {
                            Box(
                                modifier = Modifier
                                    .size(titleHeight)
                                    .background(
                                        color = MaterialTheme.colorScheme.surface,
                                        shape = CircleShape
                                    )
                            ) {
                                IconButton(onClick = onNavigateUp) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        },
                        actions = {
                            RadarSettingsOptions(
                                modifier = Modifier.widthIn(max = 320.dp),
                                radarState = radarState,
                                locationState = locationState,
                                titleHeight = titleHeight,
                                onUpdateSettingsOpen = onUpdateSettingsOpen,
                                onUpdateCrossHairVisibility = onUpdateCrossHairVisibility,
                                onUpdateElevationVisibility = onUpdateElevationVisibility,
                                onUpdateClockGaugeVisibility = onUpdateClockGaugeVisibility,
                                onUpdateRadarClock = onUpdateRadarClock,
                                onUpdateLocation = { (latitude, longitude) ->
                                    goToLocation(webView, latitude, longitude)
                                },
                            )
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Transparent
                        ),
                        windowInsets = WindowInsets(
                            left = 0, top = TopAppBarDefaults.windowInsets.getTop(density), right = 0, bottom = 0
                        )
                    )
                },
                bottomBar = {
                    AnimatedVisibility(
                        visible = radarState.clockGaugeVisible,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
                        exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            RadarClockManipulator(
                                gaugeSize = clockGaugeWidth,
                                dayListState = dayListState,
                                dayGaugeProvider = { !isDark },
                                radarState = radarRealState,
                                clockGaugeRotationCoefficient = clockGaugeRotationCoefficient,
                                silent = appSettings.silent,
                                vibration = appSettings.vibration,
                                weatherModel = selectedModel,

                                onUpdateVisibleDayIndex = remember { onUpdateVisibleDayIndex },
                                onUpdateDayGaugeIndex = remember { onUpdateDayGaugeIndex },
                                onUpdateClockGaugeRotation = remember { onUpdateClockGaugeRotation },
                                onStopRotation = remember {
                                    {  radarState.clockGaugeRotation.stop() }
                                },
                                onDecayRotation = { velocity ->
                                    radarState.clockGaugeRotation.animateDecay(
                                        initialVelocity = (velocity * clockGaugeRotationCoefficient).coerceIn(-90f, 90f),
                                        animationSpec = exponentialDecay(frictionMultiplier = 0.25f)
                                    ) {
                                        if (abs(this.velocity) < 10f) {
                                            val targetValue = (radarState.clockGaugeRotation.value / 15f).roundToInt() * 15f
                                            coroutineScope.launch {
                                                radarState.clockGaugeRotation.animateTo(targetValue)
                                            }
                                        }
                                    }
                                },
                                onUndoButtonPressed = {
                                    onBalanceClockGaugeValues()
                                    coroutineScope.launch {
                                        radarRealState.value.clockGaugeRotation.animateTo(
                                            targetValue = 0f,
                                            animationSpec = tween(durationMillis = 700)
                                        )
                                    }
                                    onResetDayGauge()
                                },
                                onUpdateClockGaugeDragStopped = remember { onUpdateClockGaugeDragStopped }
                            )
                            Spacer(
                                Modifier.windowInsetsBottomHeight(
                                    WindowInsets.systemBars
                                )
                            )
                        }
                    }
                }
            )
            { paddingValues ->
                val topPadding = paddingValues.calculateTopPadding()
                val borderColor = MaterialTheme.colorScheme.primaryContainer
                val surfaceColor = MaterialTheme.colorScheme.surface
                val webViewOrigin = "https://appassets.androidplatform.net"

                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { _ ->
                            val assetLoader = WebViewAssetLoader.Builder()
                                .addPathHandler(
                                    "/assets/",
                                    WebViewAssetLoader.AssetsPathHandler(context)
                                )
                                .build()
                            webView.apply {
                                webChromeClient = object : WebChromeClient() {
                                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                                        Log.i(
                                            "WebViewConsole",
                                            """
                                        ${consoleMessage?.message()}
                                        source: ${consoleMessage?.sourceId()}
                                        """.trimIndent()
                                        )
                                        return super.onConsoleMessage(consoleMessage)
                                    }
                                }
                                webViewClient = object : WebViewClient() {
//                                    override fun onPageFinished(view: WebView?, url: String?) {
//                                        super.onPageFinished(view, url)
//                                        isPageReady = true
//                                        Log.i("send1", "onPageFinished!")
//                                    }
                                    override fun shouldInterceptRequest(
                                        view: WebView,
                                        request: WebResourceRequest
                                    ): WebResourceResponse? = assetLoader.shouldInterceptRequest(request.url)
                                }
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true

                                addWebMessageListener(
                                    webView = this,
                                    onUpdateLoadingIndicatorVisible = onUpdateLoadingIndicatorVisible,
                                    onUpdateQuantityValue = onUpdateQuantityValue,
                                    onUpdateElevation = onUpdateElevation,
                                    onUpdateTitleAnimationState = {
                                        if (selectedQuantity != currentRadarState.quantity)
                                            titleAnimationState = it

                                        if (selectedModel != currentRadarState.quantityWeatherModels[currentRadarState.quantity])
                                            modelAnimationState = it

                                        quantityChangeSuccessful = it != TitleState.OneTitle
                                        modelChangeSuccessful = quantityChangeSuccessful
                                    },
                                    onRevertSelectedQuantity = {
                                        selectedQuantity = currentRadarState.quantity
                                        selectedModel =
                                            currentRadarState.quantityWeatherModels[currentRadarState.quantity]!!
                                    },
                                    onDisplaySnackbarMessage = { message ->
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = message,
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    },
                                    onPageReady = { isPageReady = true },
                                )

                                loadDataWithBaseURL(
                                    "$webViewOrigin/assets/", // Base URL context to fix domain relative checks
                                    modifiedRadarMap(
                                        topPadding = topPadding.value,
                                        valueBarHeight = valueBarHeight.value,
                                        borderColor = borderColor,
                                        surfaceColor = surfaceColor,
                                        isDark = isDark,
                                        centerLat = locationState.firstOrNull()?.latitude ?: 0f,
                                        centerLng = locationState.firstOrNull()?.longitude ?: 0f
                                    ),
                                    "text/html",
                                    "UTF-8",
                                    "$webViewOrigin/assets/"
                                )
//                        clearCache(true)
                            }
                        },
//                update = {}
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = topPadding),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ValueBar(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(valueBarHeight)
                                .padding(horizontal = 4.dp),
                            radarState = radarState,
                            appSettings = appSettings
                        )
                        if (radarState.elevationVisible) {
                            Elevation(
                                modifier = Modifier
                                    .height(valueBarHeight)
                                    .padding(start = 4.dp),
                                elevation = radarState.elevation?.mToUnit(appSettings.lengthUnit)
                                    ?.roundToInt(),
                                appSettings = appSettings
                            )
                        }
                    }
                    if (radarState.crossHairVisible) {
                        Image(
                            painter = painterResource(R.drawable.cross_hair3),
                            modifier = Modifier
                                .size(64.dp)
                                .align(Alignment.Center),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        )
                    }
                    val offsetY = animateIntAsState(
                        targetValue =
                            (if (radarState.clockGaugeVisible)
                                dimensionResource(R.dimen.clock_manipulator_height).toPx(density).toInt()
                            else 0) +
                                    WindowInsets.systemBars.getBottom(density) +
                                    (16.dp).toPx(density).toInt()
                    )
                    if (radarState.noDataWarningVisible) {
                        NoDataWarning(
                            modifier = Modifier.align(Alignment.BottomCenter)
                                .offset {
                                    IntOffset(x = 0, y = -offsetY.value)
                                }
                        )
                    }
                }
            }
        }
    }
}

private fun Transition.Segment<TitleState>.titleTransitionSpec(): FiniteAnimationSpec<Float> =
    when {
        TitleState.OldTitleExit isTransitioningTo TitleState.OneTitle -> snap()
        else -> tween()
    }
@Composable
private fun Title(
    modifier: Modifier = Modifier,
    animationState: TitleState,
    modelAnimationState: TitleState,
    titleHeight: Dp,
    quantity: RadarWeatherQuantity,
    weatherModel: WeatherModels,
    selectedQuantity: RadarWeatherQuantity,
    selectedWeatherModel: WeatherModels,
    loadingIndicatorVisible: Boolean,
    onUpdateQuantityDialogVisible: (Boolean) -> Unit
) {
    val animationTransition = updateTransition(animationState, label = "Title state")
    val modelAnimationTransition = updateTransition(modelAnimationState, label = "Model state")

    val titleUpScale = animationTransition.animateFloat(
        transitionSpec = { titleTransitionSpec() }
    ) { state ->
        when (state) {
            TitleState.OneTitle -> 0.4f
            TitleState.NewTitleEnter -> 0.4f
            TitleState.OldTitleExit -> 1f
        }
    }
    val titleUpAlpha = animationTransition.animateFloat(
        transitionSpec = { titleTransitionSpec() }
    ) { state ->
        when (state) {
            TitleState.OneTitle -> 0f
            TitleState.NewTitleEnter -> 0f
            TitleState.OldTitleExit -> 1f
        }
    }
    val titleDownScale = animationTransition.animateFloat(
        transitionSpec = { titleTransitionSpec() }
    ) { state ->
        when (state) {
            TitleState.OneTitle -> 1f
            TitleState.NewTitleEnter -> 1f
            TitleState.OldTitleExit -> 0.4f
        }
    }
    val titleDownAlpha = animationTransition.animateFloat(
        transitionSpec = { titleTransitionSpec() }
    ) { state ->
        when (state) {
            TitleState.OneTitle -> 1f
            TitleState.NewTitleEnter -> 0.5f
            TitleState.OldTitleExit -> 0f
        }
    }

    val modelUpScale = modelAnimationTransition.animateFloat(
        transitionSpec = { titleTransitionSpec() }
    ) { state ->
        when (state) {
            TitleState.OneTitle -> 0.4f
            TitleState.NewTitleEnter -> 0.4f
            TitleState.OldTitleExit -> 1f
        }
    }
    val modelUpAlpha = modelAnimationTransition.animateFloat(
        transitionSpec = { titleTransitionSpec() }
    ) { state ->
        when (state) {
            TitleState.OneTitle -> 0f
            TitleState.NewTitleEnter -> 0f
            TitleState.OldTitleExit -> 1f
        }
    }
    val modelDownScale = modelAnimationTransition.animateFloat(
        transitionSpec = { titleTransitionSpec() }
    ) { state ->
        when (state) {
            TitleState.OneTitle -> 1f
            TitleState.NewTitleEnter -> 1f
            TitleState.OldTitleExit -> 0.4f
        }
    }
    val modelDownAlpha = modelAnimationTransition.animateFloat(
        transitionSpec = { titleTransitionSpec() }
    ) { state ->
        when (state) {
            TitleState.OneTitle -> 1f
            TitleState.NewTitleEnter -> 0.5f
            TitleState.OldTitleExit -> 0f
        }
    }


    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(titleHeight)
            .clip(RoundedCornerShape(50))
            .clickable(
                onClick = {
                    onUpdateQuantityDialogVisible(true)
                }
            )
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(50)
            )
    ) {
        TitleContent(
            modifier = Modifier.fillMaxSize(),
            weatherModel = selectedWeatherModel,
            quantity = selectedQuantity,
            titleScale = titleUpScale,
            titleAlpha = titleUpAlpha,
            modelScale = modelUpScale,
            modelAlpha = modelUpAlpha
        )
        TitleContent(
            modifier = Modifier.fillMaxSize(),
            weatherModel = weatherModel,
            quantity = quantity,
            titleScale = titleDownScale,
            titleAlpha = titleDownAlpha,
            modelScale = modelDownScale,
            modelAlpha = modelDownAlpha
        )

        AnimatedVisibility(
            visible = loadingIndicatorVisible,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun TitleContent(
    modifier: Modifier = Modifier,
    weatherModel: WeatherModels,
    quantity: RadarWeatherQuantity,

    titleScale: State<Float>,
    titleAlpha: State<Float>,
    modelScale: State<Float>,
    modelAlpha: State<Float>,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 16.dp)
                .offset(y = (-6).dp)
                .graphicsLayer {
                    scaleX = modelScale.value
                    scaleY = modelScale.value
                    alpha = modelAlpha.value
                },
            text = weatherModel.title,
            fontSize = 10.sp,
            fontFamily = FontFamily.Default,
            color = MaterialTheme.colorScheme.primary
        )

        Box(
            modifier = Modifier
                .offset(y = 4.dp)
                .graphicsLayer {
                    scaleX = titleScale.value
                    scaleY = titleScale.value
                    alpha = titleAlpha.value
                },
        ) {
            if (quantity.nameId != null)
                Text(
                    modifier = Modifier,
                    text = stringResource(quantity.nameId),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 16.sp,
                        maxFontSize = 20.sp,
                        stepSize = 1.sp
                    )
                )
            else
                Text(
                    modifier = Modifier,
                    text = quantity.title!!,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    autoSize = TextAutoSize.StepBased(
                        minFontSize = 16.sp,
                        maxFontSize = 20.sp,
                        stepSize = 1.sp
                    )
                )
        }
    }
}

@Composable
private fun ValueBar(
    modifier: Modifier = Modifier,
    radarState: RadarState,
    appSettings: AppSettings
) {
    val density = LocalDensity.current
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        var colorBarWidth by remember { mutableStateOf(0.dp) }
        val colorBreakPoints = RadarWeatherQuantity.colorBreakpoints(radarState.quantity)
        val colorScale = RadarWeatherQuantity.colorScale(
            quantity = radarState.quantity,
            isDark = MaterialTheme.isDarkTheme
        )
//        var boxColor = MaterialTheme.colorScheme.surface

        /*radarState.quantityValue?.let { quantityValue ->
            val lowerIndex = colorBreakPoints.indexOfLast { it <= quantityValue }
            val higherIndex = colorBreakPoints.indexOfFirst { it > quantityValue }
            if (lowerIndex == -1)
                boxColor = colorScale.first()
            else if (higherIndex == -1)
                boxColor = colorScale.last()
            else {
                val fraction = lowerIndex / (colorBreakPoints.size - 1f) +
                        (quantityValue - colorBreakPoints[lowerIndex]) /
                        ((colorBreakPoints[higherIndex] - colorBreakPoints[lowerIndex]) * (colorBreakPoints.size - 1f))
                boxColor = lerpMultipleColors(colorScale, fraction)
            }
        }*/
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(72.dp)
                .border(
                    width = dimensionResource(id = R.dimen.connector_line_width),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(50)
                )
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(50)
                ),
            contentAlignment = Alignment.Center
        ) {
            BorderedText(
                text = RadarWeatherQuantity.quantityValueText(
                    quantity = radarState.quantity,
                    quantityValue = radarState.quantityValue?.applyUnit(
                        quantity = radarState.quantity,
                        appSettings = appSettings
                    ),
                    appSettings = appSettings
                ),
                fillColor = /*boxColor.mostContrastColor()*/MaterialTheme.colorScheme.onSurface,
//                strokeColor = boxColor.mostContrastColor().invert(),
                strokeWidth = 1f,
                alignment = Alignment.Center
            )
        }
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .border(
                    width = dimensionResource(id = R.dimen.connector_line_width),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(50)
                )
                .background(
                    brush = Brush.horizontalGradient(
                        colorStops = Array(size = colorScale.size) { i ->
                            i / (colorScale.size - 1).toFloat() to colorScale[i]
                        }
                    ),
                    shape = RoundedCornerShape(50)
                )
                .onSizeChanged {
                    colorBarWidth = it.width.toDp(density)
                }
        ) {
            val tickCount = floor(colorBarWidth / (48.dp)).toInt()

            repeat(tickCount) { i ->
                val indexFloat = (1 + 2*i) * (colorBreakPoints.size - 1) / (2f*tickCount)
                val index = indexFloat.toInt()
                val indexFraction = indexFloat - index
                val value = (colorBreakPoints[index] + indexFraction *
                        (colorBreakPoints[index+1] - colorBreakPoints[index])).applyUnit(
                    radarState.quantity,
                    appSettings
                )
                val valueRounded = value.roundToDecimalDigits(
                    digitCount = RadarWeatherQuantity.coloBarFloatingPointDigits(
                        radarState.quantity,
                        appSettings
                    )
                )
                if (valueRounded == 0f) return@repeat
                val tickDisplaceFraction = (valueRounded - value) /
                        ((colorBreakPoints[index+1] - colorBreakPoints[index])
                            .applyUnit(radarState.quantity, appSettings) *
                                (colorBreakPoints.size - 1)) + (1 + 2f*i) / (2*tickCount)
                val tickDisplace = tickDisplaceFraction * colorBarWidth
                val tickPlaceColor = lerpMultipleColors(colorScale, tickDisplaceFraction)
                val tickColor = tickPlaceColor.mostContrastColor()

                Column(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .align(Alignment.CenterStart)
                        .absoluteOffset(x = tickDisplace),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(color = tickColor)
                    )
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(color = tickColor)
                    )
                }
                BorderedText(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .absoluteOffset {
                            IntOffset(
                                x = (tickDisplace - colorBarWidth / 2f).toPx().toInt(),
                                y = 0
                            )
                        },
                    text = RadarWeatherQuantity.quantityValueText(
                        quantity = radarState.quantity,
                        quantityValue = valueRounded,
                        appSettings = appSettings
                    ),
                    fillColor = tickColor,
                    fontSize = 12.sp,
                    strokeWidth = 1f,
                    alignment = Alignment.Center
                )
            }
        }
        val unitText = RadarWeatherQuantity.unit(radarState.quantity, appSettings)
        if (unitText.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(72.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(50)
                    )
                    .border(
                        width = dimensionResource(id = R.dimen.connector_line_width),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(50)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = unitText,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        } else {
            Box(modifier = Modifier
                .fillMaxHeight()
                .width(72.dp))
        }
    }
}

@Composable
private fun Elevation(
    modifier: Modifier = Modifier,
    elevation: Int?,
    appSettings: AppSettings
) {
    Box(
        modifier = modifier
            .width(72.dp)
            .border(
                width = dimensionResource(id = R.dimen.connector_line_width),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(50)
            )
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(50)
            ),
        contentAlignment = Alignment.Center
    ) {
        val unit = when (appSettings.lengthUnit){
            Length.SI -> "m"
            Length.IMPERIAL -> "ft"
        }
        val text = elevation?.let { "$it $unit" } ?: "?"
        Text(
            text = text,
        )
    }

}


@Composable
private fun NoDataWarning(
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "tense")
    val tenseVisibility by infiniteTransition.animateValue(
        initialValue = 0f,
        targetValue = 1.5f,
        typeConverter = Float.VectorConverter,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "no data alpha"
    )
    Row(
        modifier = modifier.height(48.dp)
            .padding(horizontal = 8.dp)
            .fillMaxWidth().widthIn(max = 500.dp)
            .graphicsLayer {
                alpha = if (tenseVisibility < 0.5f) 0f else 1f
            }
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(50)
            )
            .border(
                width = dimensionResource(id = R.dimen.connector_line_width),
                color = MaterialTheme.customColorScheme.warning,
                shape = RoundedCornerShape(50)
            )
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.warning),
            tint = MaterialTheme.customColorScheme.warning,
            contentDescription = null
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = stringResource(R.string.no_data_for_selected_time),
            color = MaterialTheme.colorScheme.onBackground,
            fontFamily = FontFamily.Default
        )
    }

}

fun updateWeatherLayer(
    webView: WebView,
    variable: String,
    weatherModel: String,
    timeIndex: Int,
    dark: Boolean,
) {
    webView.evaluateJavascript(
        """
            window.updateWeatherLayer('$variable', '$weatherModel', '$timeIndex', 1, $dark)
                .then(function(result) {
                    console.log(result);
                })
                .catch(function(error) {
                    console.error(error);
                });
            """.trimIndent(),
        null
    )
}
fun updateElevationVisible(webView: WebView, visible: Boolean) {
    webView.evaluateJavascript(
        """
            window.updateElevationVisible('$visible');
            """.trimIndent(),
        null
    )
}
fun goToLocation(webView: WebView, latitude: Float, longitude: Float) {
    webView.evaluateJavascript(
        """
            window.goToLocation('$longitude', '$latitude');
            """.trimIndent(),
        null
    )
}



private fun addWebMessageListener(
    webView: WebView,
    onUpdateLoadingIndicatorVisible: (Boolean) -> Unit,
    onUpdateQuantityValue: (Float?) -> Unit,
    onUpdateElevation: (Float?) -> Unit,
    onUpdateTitleAnimationState: (TitleState) -> Unit,
    onRevertSelectedQuantity: () -> Unit,
    onDisplaySnackbarMessage: (message: String) -> Unit,
    onPageReady: () -> Unit,
) {
    val webViewOrigin = "https://appassets.androidplatform.net"
    if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
        WebViewCompat.removeWebMessageListener(webView, "AndroidBridge")
        WebViewCompat.removeWebMessageListener(webView, "AndroidQuantityBridge")
        WebViewCompat.removeWebMessageListener(webView, "AndroidSnackbarBridge")

        WebViewCompat.addWebMessageListener(
            webView,
            "AndroidBridge",
            setOf(webViewOrigin)
        ) { _, wMessageCompat, _, _, _ ->
            try {
                val jsonData = JSONObject(wMessageCompat.data ?: "{}")
                when (val function = jsonData.optString("function")) {
                    "updateLoadingIndicatorVisible" -> {
                        val visible = jsonData.optBoolean("value", false)
                        onUpdateLoadingIndicatorVisible(visible)
                    }
                    "updateQuantityValue" -> {
                        if (jsonData.isNull("value")) {
                            onUpdateQuantityValue(null)
                        } else {
                            val value = jsonData.optDouble("value", 0.0).toFloat()
                            onUpdateQuantityValue(value)
                        }
                        if (jsonData.isNull("elevation")) {
                            onUpdateElevation(null)
                        } else {
                            val elevation = jsonData.optDouble("elevation", -10000.0).toFloat()
                            onUpdateElevation(elevation)
                        }
                    }
                    "updateElevation" -> {
                        if (jsonData.isNull("value")) {
                            onUpdateElevation(null)
                        } else {
                            val elevation = jsonData.optDouble("value", -10000.0).toFloat()
                            onUpdateElevation(elevation)
                        }
                    }
                    "pageReady" -> {
                        onPageReady()
                        Log.i("send1", "Page is ready!")
                    }
                    else -> Log.w("WebView", "Unknown function: $function")
                }

            } catch (e: Exception) {
                Log.e("WebView", "Error parsing message: ${e.message}")
            }
        }
        WebViewCompat.addWebMessageListener(
            webView,
            "AndroidQuantityBridge",
            setOf(webViewOrigin)
        ) { _, wMessageCompat, _, _, _ ->
            try {
                onUpdateLoadingIndicatorVisible(false)
                val jsonData = JSONObject(wMessageCompat.data ?: "{}")
                if (jsonData.isNull("quantity")) {
                    onUpdateTitleAnimationState(TitleState.OneTitle)
                    onRevertSelectedQuantity()
                } else
                    onUpdateTitleAnimationState(TitleState.OldTitleExit)
            } catch (e: Exception) {
                Log.e("WebView", "Error parsing message: ${e.message}")
            }
        }
        WebViewCompat.addWebMessageListener(
            webView,
            "AndroidSnackbarBridge",
            setOf(webViewOrigin)
        ) { _, wMessageCompat, _, _, _ ->
            try {
                val jsonData = JSONObject(wMessageCompat.data ?: "{}")
                val snackbarText = jsonData.optString("text")
                if (snackbarText.isNullOrEmpty()) return@addWebMessageListener
                onDisplaySnackbarMessage(snackbarText)

                Log.i("err1", "snackbarText = $snackbarText")
            } catch (e: Exception) {
                Log.e("WebView", "Error parsing message: ${e.message}")
            }
        }
    }
}