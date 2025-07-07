package com.takaapoo.weatherer.ui.screens.settings

import android.content.Context
import android.view.View
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.compose.ui.zIndex
import androidx.core.app.NotificationManagerCompat
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.data.local.LocalDailyWeather
import com.takaapoo.weatherer.databinding.NotificationLarge2Binding
import com.takaapoo.weatherer.databinding.NotificationSmallBinding
import com.takaapoo.weatherer.domain.WeatherType
import com.takaapoo.weatherer.domain.WeatherType.Companion.calculateMoonType
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import com.takaapoo.weatherer.domain.model.HourlyDiagramTime
import com.takaapoo.weatherer.ui.notification.NotificationSender
import com.takaapoo.weatherer.ui.notification.custom_views.CurrentIcon
import com.takaapoo.weatherer.ui.notification.custom_views.CurrentValue
import com.takaapoo.weatherer.ui.notification.custom_views.DailyChart
import com.takaapoo.weatherer.ui.notification.custom_views.HourlyChart
import com.takaapoo.weatherer.ui.notification.custom_views.HourlyChartLabel
import com.takaapoo.weatherer.ui.notification.custom_views.SmallNotification
import com.takaapoo.weatherer.ui.notification.custom_views.StatusBarIcon
import com.takaapoo.weatherer.ui.screens.detail.DailyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.WeatherQuantity
import com.takaapoo.weatherer.ui.theme.Gray30
import com.takaapoo.weatherer.ui.theme.Transparent
import com.takaapoo.weatherer.utility.getColorScheme
import com.takaapoo.weatherer.utility.mostContrastColor
import com.takaapoo.weatherer.utility.removeTransparentBorder
import com.takaapoo.weatherer.utility.renderViewToBitmap
import com.takaapoo.weatherer.utility.spToFloat
import com.takaapoo.weatherer.utility.toDp
import com.takaapoo.weatherer.utility.toPx
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.min


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    modifier: Modifier = Modifier,
    appSettings: AppSettings,
    singlePane: Boolean,
    notificationHourlyChartData: List<HourlyChartDto>,
    notificationDailyChartData: List<LocalDailyWeather>,

    onUpdateNotificationVisibility: (visible: Boolean) -> Unit,
    onUpdateNotificationCurrentQuantities: (quantity: WeatherQuantity, addRemove: Boolean) -> Unit,
    onUpdateNotificationHourlyDiagramIconsVisibility: (visible: Boolean) -> Unit,
    onUpdateNotificationHourlyQuantities: (quantity: WeatherQuantity, addRemove: Boolean) -> Unit,
    onUpdateNotificationCurveShadowVisibility: (visible: Boolean) -> Unit,
    onSaveCurvesColor: (curveIndex: Int, color: Color) -> Unit,
    onUpdateWholeCurvesColor: (colors: List<Color>) -> Unit,
    onUpdateNotificationTimeAxisDialogVisibility: (visible: Boolean) -> Unit,

    onUpdateNotificationDailyWeatherIconsVisible: (visible: Boolean) -> Unit,
    onUpdateNotificationDailyQuantity: (quantity: DailyWeatherQuantity) -> Unit,
    onUpdateGridDialogVisibility: (visible: Boolean) -> Unit,

    onUpdateLocationDialogVisibility: (visible: Boolean) -> Unit,
    onUpdateNotificationWeatherIconVisibility: (visible: Boolean) -> Unit,
    onUpdateNotificationDateTimeDialogVisibility: (visible: Boolean) -> Unit,

    onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val notificationManager = NotificationManagerCompat.from(context)
    var smallNotificationVisible by rememberSaveable { mutableStateOf(false) }
    val transition = updateTransition(smallNotificationVisible, label = "small/large transition")

    val scrollState = rememberScrollState()
    val settingScrollState = rememberScrollState()
    var header by rememberSaveable { mutableIntStateOf(1) }
    var previousHeader by rememberSaveable { mutableIntStateOf(1) }
    var notificationWidth by remember { mutableIntStateOf(0) }

    val coroutineScope = rememberCoroutineScope()
    var colorBottomSheetVisible by remember { mutableStateOf(false) }
    val colorSheetState = rememberModalBottomSheetState()

    val smallNotificationHeight = 88.dp
    val settingsBoxHeight = 80.dp
    var outerColumnScrollMax = (settingsBoxHeight + 16.dp).toPx(density)

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (scrollState.value < outerColumnScrollMax) {
                    val consumeY = min(-available.y, outerColumnScrollMax - scrollState.value)
                    coroutineScope.launch {
                        scrollState.scrollBy(consumeY)
                    }
                    return Offset(0f, -consumeY) // Consume for outer scroll
                }
                return Offset.Zero // Delegate to inner
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                return if (available.y > 0) super.onPostScroll(consumed, available, source)
                else available
            }
        }
    }
    var maxHeight by remember { mutableStateOf(0.dp) }
    var notificationPartHeight by remember { mutableIntStateOf(0) }
    var settingsVisibility  = appSettings.notificationVisibility &&
            notificationManager.areNotificationsEnabled()

    var curvesColor by rememberSaveable { mutableStateOf(appSettings.notificationCurvesColor) }

    LaunchedEffect(
        key1 = appSettings.copy(
            notificationVisibility = false,
            lastNotificationHeaderSelected = 1
        ),
        key2 = notificationHourlyChartData,
        key3 = notificationDailyChartData
    ) {
        if (appSettings.notificationVisibility)
            NotificationSender(
                context = context,
                header = appSettings.lastNotificationHeaderSelected,
                notificationHourlyChartData = notificationHourlyChartData,
                notificationDailyChartData = notificationDailyChartData,
                appSettings = appSettings,
                hourlyChartQuantities = appSettings.notificationHourlyQuantities,
                currentQuantities = appSettings.notificationCurrentQuantities
            ).sendNotification()
    }

    val statusBarIcon = remember(notificationHourlyChartData, appSettings) {
        StatusBarIcon(
            context = context,
            hourlyChartData = notificationHourlyChartData,
            quantity = WeatherQuantity.TEMPERATURE,
            appSettings = appSettings
        ).apply {
            val widthSpec = View.MeasureSpec.makeMeasureSpec(
                48.dp.toPx(density).toInt(),
                View.MeasureSpec.EXACTLY
            )
            val heightSpec = View.MeasureSpec.makeMeasureSpec(
                48.dp.toPx(density).toInt(),
                View.MeasureSpec.EXACTLY
            )
            measure(widthSpec, heightSpec)
            layout(0, 0, measuredWidth, measuredHeight)
        }.let {
            renderViewToBitmap(it)
                ?.removeTransparentBorder(n = 3, minHeight = 38.dp.toPx(density).toInt())
                ?.asImageBitmap()
        }
    }

    val notificationIconTranslation by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = 300, easing = FastOutSlowInEasing)
        }
    ) {
        if (it) 16.dp.toPx(density)
        else 0f
    }
    val smallNotificationAlpha by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = 300, easing = CubicBezierEasing(0.3f, 0.7f, 0.3f, 1f))
        }
    ) {
        if (it) 1f else 0f
    }
    val largeNotificationAlpha by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = 300, easing = CubicBezierEasing(0.3f, 0.3f, 0.3f, 1f))
        }
    ) {
        if (it) 0f else 1f
    }
    val notificationArrowScale by transition.animateFloat(
        transitionSpec = {
            tween(durationMillis = 300, easing = CubicBezierEasing(0.9f, 0f, 0.1f, 1f))
        }
    ) {
        if (it) -1f
        else 1f
    }
    val boxHeightAnimation by transition.animateDp(
        transitionSpec = {
            tween(
                durationMillis = 300,
                easing = CubicBezierEasing(0.5f, 0f, 0.5f, 1f)
            )
        }
    ) {
        if (it) smallNotificationHeight + settingsBoxHeight + 16.dp * 3
        else dimensionResource(R.dimen.notification_height) +
                dimensionResource(R.dimen.notification_bottom_padding) + 56.dp +
                settingsBoxHeight + 16.dp * 3
    }


    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Persistent notification",
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    if (singlePane) {
                        IconButton(onClick = onNavigateUp) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        val notifSurfaceShape = RoundedCornerShape(
            topStart = 0.dp,
            topEnd = 0.dp,
            bottomStart = 32.dp,
            bottomEnd = 32.dp
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = paddingValues.calculateStartPadding(layoutDirection),
                    end = paddingValues.calculateEndPadding(layoutDirection),
                    top = paddingValues.calculateTopPadding()
                )
                .onSizeChanged {
                    maxHeight = it.height.toDp(density)
                }
                .verticalScroll(
                    state = scrollState,
                    flingBehavior = rememberSnapFlingBehavior(
                        snapHeight = outerColumnScrollMax,
                        snapThreshold = outerColumnScrollMax / 2,
                        scrollState = scrollState,
                        coroutineScope = coroutineScope
                    )
                )
                .nestedScroll(nestedScrollConnection)
        ) {
            Column(
                modifier = Modifier
                    .height(
                        if (settingsVisibility) boxHeightAnimation
                        else settingsBoxHeight + 16.dp * 2
                    )
                    .fillMaxWidth()
                    .shadow(
                        elevation = if (settingScrollState.canScrollBackward) 4.dp else 0.dp,
                        shape = notifSurfaceShape
                    )
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = notifSurfaceShape
                    )
                    .onSizeChanged {
                        notificationPartHeight = it.height
                    }
                    .padding(16.dp)
            ){
                SettingsBox(
                    modifier = Modifier.fillMaxWidth().height(settingsBoxHeight),
                    title = "Show notification",
                    onClick = { onUpdateNotificationVisibility(!appSettings.notificationVisibility) },
                    content = {
                        Switch(
                            modifier = Modifier.padding(end = 24.dp),
                            checked = appSettings.notificationVisibility,
                            onCheckedChange = onUpdateNotificationVisibility
                        )
                    }
                )
                if (settingsVisibility) {
                    Spacer(Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(24.dp)
                            )
                    ) {
                        Image(
                            modifier = Modifier
                                .size(56.dp)
                                .zIndex(1f)
                                .graphicsLayer {
                                    translationY = notificationIconTranslation
                                }
                                .padding(16.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                                .padding(4.dp)
                                .align(Alignment.TopStart),
                            bitmap = statusBarIcon!!,
                            contentDescription = null
                        )
                        Icon(
                            modifier = Modifier
                                .size(56.dp)
                                .zIndex(1f)
                                .graphicsLayer {
                                    translationY = notificationIconTranslation
                                }
                                .clip(CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) {
                                    smallNotificationVisible = !smallNotificationVisible
                                    if (smallNotificationVisible)
                                        header = 4
                                    else
                                        header = previousHeader
                                }
                                .padding(16.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                    shape = CircleShape
                                )
                                .graphicsLayer {
                                    scaleY = notificationArrowScale
                                }
                                .align(Alignment.TopEnd),
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = null
                        )

                        AndroidViewBinding(
                            factory = { layoutInflater, viewGroup, _ ->
                                NotificationSmallBinding.inflate(layoutInflater).apply {
                                    smallLayout(
                                        context = context,
                                        hourlyChartData = notificationHourlyChartData,
                                        appSettings = appSettings
                                    )
                                }
                            },
                            modifier = Modifier
                                .padding(horizontal = (24 + 2 * 16).dp, vertical = 20.dp)
                                .graphicsLayer { alpha = smallNotificationAlpha },
                            update = {
                                smallLayout(
                                    context = context,
                                    hourlyChartData = notificationHourlyChartData,
                                    appSettings = appSettings
                                )
                            }
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth()
                                .height(dimensionResource(R.dimen.notification_height) +
                                        dimensionResource(R.dimen.notification_bottom_padding) +
                                        56.dp)
                                .clip(RectangleShape)
                                .wrapContentHeight(
                                    align = Alignment.Top,
                                    unbounded = true
                                )
                                .graphicsLayer { alpha = largeNotificationAlpha }
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 56.dp)
                                    .fillMaxWidth()
                                    .height(56.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${
                                        context.packageName.substringAfterLast('.')
                                            .replaceFirstChar { it.uppercase() }
                                    } ",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Default,
                                    fontWeight = FontWeight(400)
                                )
                                Text(
                                    text = " ● ",
                                    fontSize = 6.sp,
                                    fontFamily = FontFamily.Default,
                                )
                                Text(
                                    text = " now",
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Default,
                                    fontWeight = FontWeight(400)
                                )
//                                    Spacer(Modifier.weight(1f))
                            }

                            AndroidViewBinding(
                                factory = { layoutInflater, viewGroup, _ ->
                                    NotificationLarge2Binding.inflate(layoutInflater)
                                        .apply {
                                            header1.setOnClickListener {
                                                header = 1
                                                previousHeader = 1
                                            }
                                            header2.setOnClickListener {
                                                header = 2
                                                previousHeader = 2
                                            }
                                            header3.setOnClickListener {
                                                header = 3
                                                previousHeader = 3
                                            }
                                        }
                                },
                                modifier = Modifier
                                    .padding(
                                        start = (24 + 2 * 16).dp,
                                        bottom = dimensionResource(R.dimen.notification_bottom_padding),
                                        end = 16.dp
                                    )
                                    .height(dimensionResource(R.dimen.notification_height))
                                    .onSizeChanged {
                                        notificationWidth = it.width
                                    },
                                update = {
                                    when (header) {
                                        1 -> {
                                            header1Layout(
                                                context = context,
                                                notificationHourlyChartData = notificationHourlyChartData,
                                                appSettings = appSettings,
                                                density = density
                                            )
                                        }
                                        2 -> {
                                            header2Layout(
                                                context = context,
                                                notificationHourlyChartData = notificationHourlyChartData,
                                                appSettings = appSettings,
                                                curvesColor = curvesColor,
                                                notificationWidth = notificationWidth,
                                                density = density,
                                            )
                                        }
                                        3 -> {
                                            header3Layout(
                                                context = context,
                                                appSettings = appSettings,
                                                notificationDailyChartData = notificationDailyChartData,
                                                notificationWidth = notificationWidth,
                                                density = density,
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            if (settingsVisibility) {
                AnimatedContent(
                    targetState = header,
                    modifier = Modifier.height(maxHeight)
                ) {visibleHeader ->
                    when (visibleHeader) {
                        1 -> Header1Settings(
                            modifier = Modifier.fillMaxSize(),
                            notificationCurrentQuantities = appSettings.notificationCurrentQuantities,
                            paddingValues = paddingValues,
                            scrollState = settingScrollState,
                            outerColumnScrollState = scrollState,
                            spacerInitialHeight = notificationPartHeight.toDp(density),
                            onUpdateNotificationCurrentQuantities = onUpdateNotificationCurrentQuantities
                        )
                        2 -> Header2Settings(
                            modifier = Modifier.fillMaxSize(),
                            paddingValues = paddingValues,
                            scrollState = settingScrollState,
                            outerColumnScrollState = scrollState,
                            spacerInitialHeight = notificationPartHeight.toDp(density),
                            appSettings = appSettings,
                            onUpdateNotificationHourlyDiagramIconsVisibility =
                                onUpdateNotificationHourlyDiagramIconsVisibility,
                            onUpdateNotificationHourlyQuantities = onUpdateNotificationHourlyQuantities,
                            onUpdateNotificationCurveShadowVisibility =
                                onUpdateNotificationCurveShadowVisibility,
                            onUpdateColorBottomSheetVisible = {
                                colorBottomSheetVisible = it
                            },
                            onUpdateNotificationTimeAxisDialogVisibility = onUpdateNotificationTimeAxisDialogVisibility,
                        )
                        3 -> Header3Settings(
                            modifier = Modifier.fillMaxSize(),
                            paddingValues = paddingValues,
                            scrollState = settingScrollState,
                            outerColumnScrollState = scrollState,
                            spacerInitialHeight = notificationPartHeight.toDp(density),
                            appSettings = appSettings,
                            onUpdateNotificationDailyWeatherIconsVisible =
                                onUpdateNotificationDailyWeatherIconsVisible,
                            onUpdateNotificationDailyQuantity = onUpdateNotificationDailyQuantity,
                            onUpdateGridDialogVisibility = onUpdateGridDialogVisibility,
                        )
                        4 -> SmallNotificationSettings(
                            modifier = Modifier.fillMaxSize(),
                            paddingValues = paddingValues,
                            scrollState = settingScrollState,
                            outerColumnScrollState = scrollState,
                            spacerInitialHeight = notificationPartHeight.toDp(density),
                            appSettings = appSettings,
                            locationName = notificationHourlyChartData.getOrNull(0)?.locationName
                                ?: "",
                            onUpdateLocationDialogVisibility = onUpdateLocationDialogVisibility,
                            onUpdateNotificationWeatherIconVisibility = onUpdateNotificationWeatherIconVisibility,
                            onUpdateNotificationDateTimeDialogVisibility = onUpdateNotificationDateTimeDialogVisibility,
                        )
                    }
                }
            }
        }

        if (colorBottomSheetVisible) {
            ModalBottomSheet(
                sheetState = colorSheetState,
                onDismissRequest = {
                    colorBottomSheetVisible = false
                }
            ) {
                var selectedButton by rememberSaveable { mutableIntStateOf(0) }
                var colorInitializer by rememberSaveable { mutableIntStateOf(100) }
                var selectedButtonColor by rememberSaveable(selectedButton) {
                    mutableIntStateOf(appSettings.notificationCurvesColor[selectedButton])
                }
                var initialColor by remember(selectedButton) {
                    mutableStateOf(Color(appSettings.notificationCurvesColor[selectedButton]))
                }
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(horizontal = 8.dp)
                ) {
                    val items = listOf("C1", "C2")
                    items.forEachIndexed { index, curve ->
                        val shape = SegmentedButtonDefaults.itemShape(index = index, count = items.size)
                        SegmentedButton(
                            shape = shape,
                            onClick = {
                                if (selectedButton != index)
                                    colorInitializer = index
                                selectedButton = index
                            },
                            colors = SegmentedButtonDefaults.colors().copy(
                                activeContainerColor = Color(selectedButtonColor),
                                inactiveContainerColor = Color(appSettings.notificationCurvesColor[index]),
                                inactiveBorderColor = Transparent,
                                activeContentColor = Color(selectedButtonColor).mostContrastColor(),
                                inactiveContentColor = Color(appSettings.notificationCurvesColor[index]).mostContrastColor(),
                            ),
                            selected = selectedButton == index
                        ) {
                            Text(
                                text = curve,
                                fontFamily = FontFamily.Default,
                                fontWeight = FontWeight.W500,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                Box(modifier = Modifier.fillMaxWidth()) {
                    PickColor(
                        modifier = Modifier
                            .height(288.dp)
                            .fillMaxWidth(),
                        initialColor = initialColor,
                        initializeColor = colorInitializer,
                        onModifyColor = {
                            val colorInt = it.toArgb()
                            curvesColor = curvesColor.toMutableList().apply {
                                set(selectedButton, colorInt)
                            }
                            selectedButtonColor = colorInt
                        },
                        onSaveCurvesColor = {
                            onSaveCurvesColor(selectedButton, it)
                        }
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .padding(horizontal = 32.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    val listState = rememberLazyListState()
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
//                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        contentPadding = PaddingValues(top = 4.dp, start = 8.dp, end = 8.dp),
                        flingBehavior = rememberSnapFlingBehavior(
                            lazyListState = listState,
                            snapPosition = SnapPosition.Start
                        ),
                        state = listState
                    ) {
                        items(
                            items = predefinedHourlyCurveColor.map {
                                HourlyCurveColor(
                                    setId = it.setId,
                                    colors = it.colors.subList(1, 3)
                                )
                            },
                            key = { item -> item.setId }
                        ) { curveColorSet ->
                            ColorSetRow(
                                modifier = Modifier
                                    .height(48.dp)
                                    .widthIn(max = 240.dp),
                                colorSet = curveColorSet,
                                onUpdateWholeCurvesColor = { colors ->
                                    onUpdateWholeCurvesColor(colors)
                                    initialColor = colors[selectedButton]
                                    colorInitializer++
                                }
                            )
                        }
                        item {
                            Spacer(Modifier.height(20.dp))
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .shadow(elevation = 1.dp, shape = SlotShadowShape())
                    ){
                        Box(modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(color = Gray30)
                            .align(Alignment.TopCenter)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(24.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                brush = Brush.verticalGradient(
                                    listOf(
                                        BottomSheetDefaults.ContainerColor.copy(alpha = 0f),
                                        BottomSheetDefaults.ContainerColor
                                    )
                                )
                            )
                    )
                }
            }
        }
    }
}

private fun NotificationSmallBinding.smallLayout(
    context: Context,
    hourlyChartData: List<HourlyChartDto>,
    appSettings: AppSettings,
){
    val zone: ZoneId = when (appSettings.notificationDateTime) {
        HourlyDiagramTime.UTC -> ZoneId.of("UTC")
        HourlyDiagramTime.DEVICE -> ZoneId.systemDefault()
        HourlyDiagramTime.LOCATION -> {
            ZoneOffset.ofTotalSeconds((hourlyChartData.getOrNull(0)?.utcOffset?.toInt() ?: 0) * 60)
        }
    }

    val now: LocalDateTime = LocalDateTime.now(zone)
    val dayOfWeek: String = now.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    val month = now.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase()
    val dayOfMonth = now.dayOfMonth
    val clockText: String = now.format(DateTimeFormatter.ofPattern("HH\u202F:\u202Fmm"))
    val dateText = "$dayOfWeek., $month. $dayOfMonth,"

    val hourlyWeather = hourlyChartData.find {
        it.hourlyWeather?.time == LocalDateTime.now(ZoneId.of("UTC")).truncatedTo(ChronoUnit.HOURS).toString()
    }?.hourlyWeather
//    moonPhaseMap = Json.parseToJsonElement(readJSONFromAssets(context, "moon_phase.json"))
//        .jsonObject.toMap()
    val weatherType = WeatherType.fromWMO(
        code = hourlyWeather?.weatherCode,
        isDay = hourlyWeather?.isDay != 0,
        moonType = calculateMoonType(now.toLocalDate())
    )


    val smallNotification = SmallNotification(
        context = context,
        hourlyChartData = hourlyChartData.toImmutableList(),
        weatherType = weatherType,
        appSettings = appSettings
    ).apply {
        val widthSpec = View.MeasureSpec.makeMeasureSpec(
            context.resources.getDimension(R.dimen.notification_temperature_width).toInt(),
            View.MeasureSpec.EXACTLY
        )
        val heightSpec = View.MeasureSpec.makeMeasureSpec(
            context.resources.getDimension(R.dimen.notification_weather_type_size).toInt(),
            View.MeasureSpec.EXACTLY
        )
        measure(widthSpec, heightSpec)
        layout(0, 0, measuredWidth, measuredHeight)
    }
    val bitmap = renderViewToBitmap(smallNotification)

    locationName.text = hourlyChartData.getOrNull(0)?.locationName ?: ""
    date.text = dateText
    clock.text = clockText
    temperature.setImageBitmap(bitmap)
}

private fun NotificationLarge2Binding.header1Layout(
    context: Context,
    notificationHourlyChartData: List<HourlyChartDto>,
    appSettings: AppSettings,
    density: Density,
){
    val iconHeight = context.resources.getDimension(R.dimen.notification_icon_size)
    val iconWidth = context.resources.getDimension(R.dimen.notification_icon_width_with_point)
    val valueHeight = context.resources.getDimension(R.dimen.notification_icon_value_height)
    val valueWidth = context.resources.getDimension(R.dimen.notification_icon_value_width)

    val colorScheme = getColorScheme(context, appSettings.dynamicColor)
    val iconViews = listOf(icon1, icon2, icon3, icon4, icon5, icon6, icon7)
    val iconValues = listOf(icon1Value, icon2Value, icon3Value, icon4Value, icon5Value,
        icon6Value, icon7Value)
    val iconConnectors = listOf(icon1Connector, icon2Connector, icon3Connector, icon4Connector,
        icon5Connector, icon6Connector, icon7Connector)
    val quantityList = listOf(quantity1, quantity2, quantity3, quantity4, quantity5, quantity6,
        quantity7)
    quantityList.forEach {
        it.visibility = View.GONE
    }

    appSettings.notificationCurrentQuantities.forEachIndexed { i, quantity ->
        val icon = CurrentIcon(
            context = context,
            iconId = quantity.iconId,
            colorScheme = colorScheme
        ).apply {
            val widthSpec = View.MeasureSpec.makeMeasureSpec(
                iconWidth.toInt(),
                View.MeasureSpec.EXACTLY
            )
            val heightSpec = View.MeasureSpec.makeMeasureSpec(
                iconHeight.toInt(),
                View.MeasureSpec.EXACTLY
            )
            measure(widthSpec, heightSpec)
            layout(0, 0, measuredWidth, measuredHeight)
        }
        val iconBitmap = renderViewToBitmap(icon)

        val value = CurrentValue(
            context = context,
            hourlyChartData = notificationHourlyChartData,
            chartQuantity = quantity,
            appSettings = appSettings,
            fontSize = 14.sp.toPx(density)
        ).apply {
            val widthSpec = View.MeasureSpec.makeMeasureSpec(
                valueWidth.toInt(),
                View.MeasureSpec.EXACTLY
            )
            val heightSpec = View.MeasureSpec.makeMeasureSpec(
                valueHeight.toInt(),
                View.MeasureSpec.EXACTLY
            )
            measure(widthSpec, heightSpec)
            layout(0, 0, measuredWidth, measuredHeight)
        }
        val valueBitmap = renderViewToBitmap(value)

        quantityList[i].visibility = View.VISIBLE
        iconViews[i].setImageBitmap(iconBitmap)
        iconConnectors[i].setImageResource(R.drawable.notif_connector)
        iconConnectors[i].setColorFilter(colorScheme.primaryContainer.toArgb())
        iconValues[i].setImageBitmap(valueBitmap)
    }

    quantities.visibility = View.GONE
    diagram.visibility = View.GONE
    dailyDiagram.visibility = View.GONE
    header1Indicator.visibility = View.VISIBLE
    header2Indicator.visibility = View.INVISIBLE
    header3Indicator.visibility = View.INVISIBLE

    header1.setTextColor(colorScheme.onSurface.toArgb())
    header2.setTextColor(colorScheme.onSurfaceVariant.toArgb())
    header3.setTextColor(colorScheme.onSurfaceVariant.toArgb())

    header1Indicator.setBackgroundColor(colorScheme.primary.toArgb())
    divider.setBackgroundColor(colorScheme.outlineVariant.toArgb())
}

private fun NotificationLarge2Binding.header2Layout(
    context: Context,
    notificationHourlyChartData: List<HourlyChartDto>,
    appSettings: AppSettings,
    curvesColor: List<Int>,
    notificationWidth: Int,
    density: Density,
){
    val labelWidth = notificationWidth -
            context.resources.getDimension(R.dimen.notification_hour_diagram_label_margin_start)
    val labelHeight = context.resources.getDimension(R.dimen.notification_hour_diagram_label_height)
    val diagramHeight = context.resources.getDimension(R.dimen.notification_diagram_height)

    val colorScheme = getColorScheme(context, appSettings.dynamicColor)

    val hourlyChartLabel = HourlyChartLabel(
        context = context,
        hourlyChartData = notificationHourlyChartData,
        chartQuantities = appSettings.notificationHourlyQuantities,
        appSettings = appSettings,
        curvesColor = curvesColor,
        fontSize = spToFloat(context, 14f)
    ).apply {
        val widthSpec = View.MeasureSpec.makeMeasureSpec(labelWidth.toInt(), View.MeasureSpec.EXACTLY)
        val heightSpec = View.MeasureSpec.makeMeasureSpec(labelHeight.toInt(), View.MeasureSpec.EXACTLY)
        measure(widthSpec, heightSpec)
        layout(0, 0, measuredWidth, measuredHeight)
    }
    val labelBitmap = renderViewToBitmap(hourlyChartLabel)

    val hourlyChart = HourlyChart(
        context = context,
        hourlyChartData = notificationHourlyChartData.toImmutableList(),
        diagramHorPadding = 16.dp.toPx(density),
        diagramVertPadding = 16.dp.toPx(density),
        chartQuantities = appSettings.notificationHourlyQuantities,
        appSettings = appSettings,
        curvesColor = curvesColor,
    ).apply {
        val widthSpec = View.MeasureSpec.makeMeasureSpec(
            notificationWidth,
            View.MeasureSpec.EXACTLY
        )
        val heightSpec = View.MeasureSpec.makeMeasureSpec(
            diagramHeight.toInt(),
            View.MeasureSpec.EXACTLY
        )
        measure(widthSpec, heightSpec)
        layout(0, 0, measuredWidth, measuredHeight)
    }
    val diagramBitmap = renderViewToBitmap(hourlyChart)

    quantity1.visibility = View.GONE
    quantity2.visibility = View.GONE
    quantity3.visibility = View.GONE
    quantity4.visibility = View.GONE
    quantity5.visibility = View.GONE
    quantity6.visibility = View.GONE
    quantity7.visibility = View.GONE
    quantities.visibility = View.VISIBLE
    diagram.visibility = View.VISIBLE
    dailyDiagram.visibility = View.GONE
    header1Indicator.visibility = View.INVISIBLE
    header2Indicator.visibility = View.VISIBLE
    header3Indicator.visibility = View.INVISIBLE

    header2.setTextColor(colorScheme.onSurface.toArgb())
    header1.setTextColor(colorScheme.onSurfaceVariant.toArgb())
    header3.setTextColor(colorScheme.onSurfaceVariant.toArgb())

    header2Indicator.setBackgroundColor(colorScheme.primary.toArgb())
    divider.setBackgroundColor(colorScheme.outlineVariant.toArgb())

    quantities.setImageBitmap(labelBitmap)
    diagram.setImageBitmap(diagramBitmap)
}

private fun NotificationLarge2Binding.header3Layout(
    context: Context,
    notificationDailyChartData: List<LocalDailyWeather>,
    appSettings: AppSettings,
    notificationWidth: Int,
    density: Density,
){
    val notificationHeight = context.resources.getDimension(R.dimen.notification_height)
    val headerHeight = context.resources.getDimension(R.dimen.notification_header_height)
    val headerMargin = context.resources.getDimension(R.dimen.notification_header_bottom_margin)
    val diagramHeight = notificationHeight - (headerHeight + headerMargin)
    val colorScheme = getColorScheme(context, appSettings.dynamicColor)

    val dailyChart = DailyChart(
        context = context,
        dailyChartData = notificationDailyChartData,
        diagramHorPadding = 16.dp.toPx(density),
        diagramVertPadding = 16.dp.toPx(density),
        appSettings = appSettings
    ).apply {
        val widthSpec = View.MeasureSpec.makeMeasureSpec(
            notificationWidth,
            View.MeasureSpec.EXACTLY
        )
        val heightSpec = View.MeasureSpec.makeMeasureSpec(
            diagramHeight.toInt(),
            View.MeasureSpec.EXACTLY
        )
        measure(widthSpec, heightSpec)
        layout(0, 0, measuredWidth, measuredHeight)
    }
    val diagramBitmap = renderViewToBitmap(dailyChart)

    quantity1.visibility = View.GONE
    quantity2.visibility = View.GONE
    quantity3.visibility = View.GONE
    quantity4.visibility = View.GONE
    quantity5.visibility = View.GONE
    quantity6.visibility = View.GONE
    quantity7.visibility = View.GONE
    quantities.visibility = View.GONE
    diagram.visibility = View.GONE
    dailyDiagram.visibility = View.VISIBLE
    header1Indicator.visibility = View.INVISIBLE
    header2Indicator.visibility = View.INVISIBLE
    header3Indicator.visibility = View.VISIBLE

    header2.setTextColor(colorScheme.onSurface.toArgb())
    header1.setTextColor(colorScheme.onSurfaceVariant.toArgb())
    header3.setTextColor(colorScheme.onSurfaceVariant.toArgb())

    header2Indicator.setBackgroundColor(colorScheme.primary.toArgb())
    divider.setBackgroundColor(colorScheme.outlineVariant.toArgb())

    dailyDiagram.setImageBitmap(diagramBitmap)
}

@Composable
fun Header1Settings(
    modifier: Modifier = Modifier,
    notificationCurrentQuantities: List<WeatherQuantity>,
    paddingValues: PaddingValues,
    scrollState: ScrollState,
    outerColumnScrollState: ScrollState,
    spacerInitialHeight: Dp,
    onUpdateNotificationCurrentQuantities: (quantity: WeatherQuantity, addRemove: Boolean) -> Unit
) {
    val density = LocalDensity.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        SettingsGroupTitle(
            modifier = Modifier.fillMaxWidth(),
            title = "Weather quantities (${notificationCurrentQuantities.size} out of 7)"
        )
        (listOf(WeatherQuantity.TEMPERATURE) + WeatherQuantity.entries.filter { it.homeCardNameId != null })
            .forEach { quantity ->
                SettingsRow1(
                    modifier = Modifier.fillMaxWidth(),
                    icon = quantity.iconId,
                    title = stringResource(quantity.homeCardNameId ?: quantity.nameId!!),
                    onClick = {
                        onUpdateNotificationCurrentQuantities(
                            quantity,
                            !notificationCurrentQuantities.contains(quantity)
                        )
                    },
                    content = {
                        Checkbox(
                            checked = notificationCurrentQuantities.contains(quantity),
                            onCheckedChange = { onUpdateNotificationCurrentQuantities(quantity, it) }
                        )
                    }
                )
            }
        Spacer(
            modifier = Modifier.height(
                paddingValues.calculateBottomPadding() + spacerInitialHeight -
                        outerColumnScrollState.value.toDp(density)
            )
        )
    }
}

@Composable
fun Header2Settings(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    scrollState: ScrollState,
    outerColumnScrollState: ScrollState,
    spacerInitialHeight: Dp,
    appSettings: AppSettings,

    onUpdateNotificationHourlyDiagramIconsVisibility: (visible: Boolean) -> Unit,
    onUpdateNotificationHourlyQuantities: (quantity: WeatherQuantity, addRemove: Boolean) -> Unit,
    onUpdateNotificationCurveShadowVisibility: (visible: Boolean) -> Unit,
    onUpdateColorBottomSheetVisible: (visible: Boolean) -> Unit,
    onUpdateNotificationTimeAxisDialogVisibility: (visible: Boolean) -> Unit
) {
    val density = LocalDensity.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        SettingsGroupTitle(
            modifier = Modifier.fillMaxWidth(),
            title = "Diagram"
        )
        SettingsRow1(
            modifier = Modifier.fillMaxWidth(),
            icon = R.drawable.partly_cloudy_day_24px,
            title = "Weather condition",
            onClick = {
                onUpdateNotificationHourlyDiagramIconsVisibility(!appSettings.notificationHourlyDiagramIconsVisibility)
            },
            content = {
                Switch(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    checked = appSettings.notificationHourlyDiagramIconsVisibility,
                    onCheckedChange = onUpdateNotificationHourlyDiagramIconsVisibility
                )
            }
        )
        SettingsRow1(
            modifier = Modifier.fillMaxWidth(),
            icon = R.drawable.clock_icon,
            title = "Time axis clock",
            subTitle = stringResource(appSettings.notificationHourlyDiagramTime.title),
            onClick = { onUpdateNotificationTimeAxisDialogVisibility(true) },
            content = {}
        )

        SettingsGroupTitle(
            modifier = Modifier.fillMaxWidth(),
            title = "Curves"
        )
        SettingsRow1(
            modifier = Modifier.fillMaxWidth(),
            icon = R.drawable.shadow_on,
            title = "Shadow",
            subTitle = if (appSettings.notificationHourlyCurveShadowVisible) "Visible" else "Invisible",
            onClick = {
                onUpdateNotificationCurveShadowVisibility(!appSettings.notificationHourlyCurveShadowVisible)
            },
            content = {
                Switch(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    checked = appSettings.notificationHourlyCurveShadowVisible,
                    onCheckedChange = onUpdateNotificationCurveShadowVisibility
                )
            }
        )
        SettingsRow3(
            modifier = Modifier.fillMaxWidth(),
            icon = R.drawable.colorize_24px,
            title = "Color",
            onClick = {
                onUpdateColorBottomSheetVisible(true)
            },
            subContent = {
                Box {
                    val circleSize = 24.dp
                    appSettings.notificationCurvesColor.forEachIndexed { index, color ->
                        Box(modifier = Modifier
                            .size(circleSize)
                            .align(Alignment.TopStart)
                            .offset(x = circleSize * index.coerceAtLeast(0) - 8.dp * index)
                            .background(color = Color(color), shape = CircleShape)
                        )
                    }
                }
            }
        )

        SettingsGroupTitle(
            modifier = Modifier.fillMaxWidth(),
            title = "Weather quantities (${appSettings.notificationHourlyQuantities.size} out of 2)"
        )
        WeatherQuantity.entries.forEach { quantity ->
            SettingsRow1(
                modifier = Modifier.fillMaxWidth(),
                icon = quantity.iconId,
                title = quantity.nameId?.let { stringResource(it) },
                titleAnnotated = quantity.title,
                onClick = {
                    onUpdateNotificationHourlyQuantities(
                        quantity,
                        !appSettings.notificationHourlyQuantities.contains(quantity)
                    )
                },
                content = {
                    Checkbox(
                        checked = appSettings.notificationHourlyQuantities.contains(quantity),
                        onCheckedChange = { onUpdateNotificationHourlyQuantities(quantity, it) }
                    )
                }
            )
        }
        Spacer(
            modifier = Modifier.height(
                paddingValues.calculateBottomPadding() + spacerInitialHeight -
                        outerColumnScrollState.value.toDp(density)
            )
        )
    }
}

@Composable
fun Header3Settings(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    scrollState: ScrollState,
    outerColumnScrollState: ScrollState,
    spacerInitialHeight: Dp,
    appSettings: AppSettings,

    onUpdateNotificationDailyWeatherIconsVisible: (visible: Boolean) -> Unit,
    onUpdateNotificationDailyQuantity: (quantity: DailyWeatherQuantity) -> Unit,
    onUpdateGridDialogVisibility: (visible: Boolean) -> Unit
) {
    val density = LocalDensity.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        SettingsGroupTitle(
            modifier = Modifier.fillMaxWidth(),
            title = "Diagram"
        )
        SettingsRow1(
            modifier = Modifier.fillMaxWidth(),
            icon = R.drawable.partly_cloudy_day_24px,
            title = "Weather condition",
            onClick = {
                onUpdateNotificationDailyWeatherIconsVisible(!appSettings.notificationDailyWeatherIconsVisible)
            },
            content = {
                Switch(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    checked = appSettings.notificationDailyWeatherIconsVisible,
                    onCheckedChange = onUpdateNotificationDailyWeatherIconsVisible
                )
            }
        )
        SettingsRow1(
            modifier = Modifier.fillMaxWidth(),
            icon = R.drawable.grid_show,
            title = stringResource(R.string.diagram_grid),
            subTitle = stringResource(appSettings.notificationDailyChartGrid.nameId),
            onClick = { onUpdateGridDialogVisibility(true) },
            content = {}
        )

        SettingsGroupTitle(
            modifier = Modifier.fillMaxWidth(),
            title = "Weather quantity"
        )
        DailyWeatherQuantity.entries.filterNot { it == DailyWeatherQuantity.SUNRISESET }
            .forEach { quantity ->
                SettingsRow1(
                    modifier = Modifier.fillMaxWidth(),
                    icon = quantity.iconId,
                    title = stringResource(quantity.nameId),
                    onClick = {
                        onUpdateNotificationDailyQuantity(quantity)
                    },
                    content = {
                        RadioButton(
                            selected = appSettings.notificationDailyQuantity == quantity,
                            onClick = { onUpdateNotificationDailyQuantity(quantity) }
                        )
                    }
                )
            }
        Spacer(
            modifier = Modifier.height(
                paddingValues.calculateBottomPadding() + spacerInitialHeight -
                        outerColumnScrollState.value.toDp(density)
            )
        )
    }
}

@Composable
fun SmallNotificationSettings(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues,
    scrollState: ScrollState,
    outerColumnScrollState: ScrollState,
    spacerInitialHeight: Dp,
    appSettings: AppSettings,
    locationName: String,

    onUpdateLocationDialogVisibility: (visible: Boolean) -> Unit,
    onUpdateNotificationWeatherIconVisibility: (visible: Boolean) -> Unit,
    onUpdateNotificationDateTimeDialogVisibility: (visible: Boolean) -> Unit,
) {
    val density = LocalDensity.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        SettingsRow1(
            modifier = Modifier.fillMaxWidth(),
            icon = R.drawable.location_on_24px,
            title = "Location",
            subTitle = locationName,
            onClick = {
                onUpdateLocationDialogVisibility(true)
            },
            content = {
            }
        )
        SettingsRow1(
            modifier = Modifier.fillMaxWidth(),
            icon = R.drawable.partly_cloudy_day_24px,
            title = "Weather condition",
            onClick = {
                onUpdateNotificationWeatherIconVisibility(!appSettings.notificationWeatherIconVisible)
            },
            content = {
                Switch(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    checked = appSettings.notificationWeatherIconVisible,
                    onCheckedChange = onUpdateNotificationWeatherIconVisibility
                )
            }
        )
        SettingsRow1(
            modifier = Modifier.fillMaxWidth(),
            icon = R.drawable.clock_icon,
            title = "Date & Time",
            subTitle = stringResource(appSettings.notificationDateTime.title),
            onClick = { onUpdateNotificationDateTimeDialogVisibility(true) },
            content = {}
        )

        Spacer(
            modifier = Modifier.height(
                paddingValues.calculateBottomPadding() + spacerInitialHeight -
                        outerColumnScrollState.value.toDp(density)
            )
        )
    }
}




@Composable
fun rememberSnapFlingBehavior(
    snapHeight: Float,
    snapThreshold: Float,
    scrollState: ScrollState,
    coroutineScope: CoroutineScope
): FlingBehavior {
    return remember(scrollState, snapThreshold) {
        object : FlingBehavior {
            override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
                return when {
                    scrollState.value >= snapThreshold && scrollState.value < snapHeight -> {
                        coroutineScope.launch {
                            scrollState.animateScrollTo(
                                value = snapHeight.toInt(),
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                        }
                        0f
                    }
                    scrollState.value < snapThreshold -> {
                        coroutineScope.launch {
                            scrollState.animateScrollTo(
                                value = 0,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioLowBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            )
                        }
                        0f
                    }
                    else -> {
                        initialVelocity
                    }
                }
            }
        }
    }
}




/*

@Preview(showBackground = true)
@Composable
fun NotificationSettingsScreenPreview() {
    WeathererTheme {
        NotificationSettingsScreen(
            modifier = Modifier.fillMaxSize(),
            appSettings = AppSettings(),
            singlePane = true,
            notificationHourlyChartData = emptyList(),
            onUpdateNotificationVisibility = {},
            onUpdateNotificationCurrentQuantities = { _, _ -> },
            onNavigateUp = {},
            notificationDailyChartData = emptyList(),
            onUpdateNotificationHourlyDiagramIconsVisibility = {},
            onUpdateNotificationHourlyQuantities = { _, _ -> },
            onUpdateNotificationCurveShadowVisibility = {},
            onSaveCurvesColor = {_, _ -> },
            onUpdateWholeCurvesColor = { },
        )
    }
}*/
