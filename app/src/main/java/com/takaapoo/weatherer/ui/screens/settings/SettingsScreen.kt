package com.takaapoo.weatherer.ui.screens.settings

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.takaapoo.weatherer.data.local.LocalDailyWeather
import com.takaapoo.weatherer.data.local.Location
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.AppTheme
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import com.takaapoo.weatherer.domain.model.HourlyChartState
import com.takaapoo.weatherer.domain.model.HourlyDiagramTime
import com.takaapoo.weatherer.domain.model.LocationsState
import com.takaapoo.weatherer.domain.model.SettingsPaneContent
import com.takaapoo.weatherer.domain.model.SettingsState
import com.takaapoo.weatherer.domain.unit.Length
import com.takaapoo.weatherer.domain.unit.Pressure
import com.takaapoo.weatherer.domain.unit.Speed
import com.takaapoo.weatherer.domain.unit.Temperature
import com.takaapoo.weatherer.ui.screens.detail.ChooseDiagramThemeDialog
import com.takaapoo.weatherer.ui.screens.detail.DailyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.HourlyWeatherQuantity
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartGrids
import com.takaapoo.weatherer.ui.screens.detail.hourly_diagram.ChartTheme
import com.takaapoo.weatherer.ui.screens.settings.dialogs.ChooseDiagramGridDialog
import com.takaapoo.weatherer.ui.screens.settings.dialogs.ChooseLocationDialog
import com.takaapoo.weatherer.ui.screens.settings.dialogs.ChooseThemeDialog
import com.takaapoo.weatherer.ui.screens.settings.dialogs.ChooseTimeAxisDialog
import com.takaapoo.weatherer.ui.theme.WeathererTheme
import com.takaapoo.weatherer.ui.theme.customColorScheme
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    appSettings: AppSettings,
    settingsState: SettingsState,
    singlePane: Boolean,
    itemState: LocationsState,
    hourlyChartData: ImmutableList<HourlyChartDto>,
    hourlyChartState: HourlyChartState,
    notificationHourlyChartData: List<HourlyChartDto>,
    notificationDailyChartData: List<LocalDailyWeather>,
    allLocations: List<Location>,
    drawerOpen: Boolean,

    onUpdateVibration: (vibrate: Boolean) -> Unit,
    onUpdateSilence: (silent: Boolean) -> Unit,
    onUpdateScreenOn: (screenOn: Boolean) -> Unit,
    onUpdateThemeDialogVisibility: (visible: Boolean) -> Unit,
    onUpdateTheme: (theme: AppTheme) -> Unit,
    onUpdateChartTheme: (chartTheme: ChartTheme) -> Unit,
    onUpdateChartGrid: (grid: ChartGrids) -> Unit,
    onUpdateDynamicTheme: (dynamic: Boolean) -> Unit,
    onUpdateTemperatureUnit: (unit: Temperature) -> Unit,
    onUpdateLengthUnit: (unit: Length) -> Unit,
    onUpdatePressureUnit: (unit: Pressure) -> Unit,
    onUpdateSpeedUnit: (unit: Speed) -> Unit,
    onUpdateTimeAxis: (time: HourlyDiagramTime) -> Unit,
    onUpdateClockGaugeVisibility: (visible: Boolean) -> Unit,
    onUpdateNotificationVisibility: (visible: Boolean) -> Unit,
    onUpdatePaneContent: (content: SettingsPaneContent, previousContent: SettingsPaneContent?) -> Unit,
    onUpdateDiagramThemeDialogVisibility: (visible: Boolean) -> Unit,
    onUpdateCardWeatherQuantities: (quantity: HourlyWeatherQuantity, add: Boolean) -> Unit,
    onUpdateHomeCardQuantityUnitsVisibility: (visible: Boolean) -> Unit,
    onUpdateInitialXAxisBounds: (Dp) -> Unit,
    onAddYAxis: (start: Float, end: Float, diagramHeight: Float,
                 textMeasurer: TextMeasurer, axisIndex: Int?, curveAnimatorInitialValue: Float) -> Unit,
    onMoveHourlyDiagramAxis: (Offset) -> Unit,
    onScaleHourlyDiagramAxis: (Offset, Float, Float) -> Unit,
    onCalculateHorizontalBarSeparation: (Float, Float, Float, TextMeasurer) -> Pair<Float, Float>,
    onCalculateVerticalBarSeparation: (Float, Float, Float, TextMeasurer) -> Pair<Int, Float>,
    onUpdateHourlyChartDotsOnCurveVisibility: (visible: Boolean) -> Unit,
    onUpdateHourlyChartCurveShadowVisibility: (visible: Boolean) -> Unit,
    onUpdateHourlyChartWeatherIconVisibility: (visible: Boolean) -> Unit,
    onUpdateHourlyChartSunRiseSetIconsVisibility: (visible: Boolean) -> Unit,
    onUpdateTimeAxisDialogVisibility: (visible: Boolean) -> Unit,
    onUpdateGridDialogVisibility: (visible: Boolean) -> Unit,
    onUpdateCurvesColor: (curveIndex: Int, color: Int) -> Unit,
    onUpdateWholeCurvesColor: (colors: List<Color>) -> Unit,
    onSaveCurvesColor: (curveIndex: Int, color: Color) -> Unit,
    onUpdateCurvesSmoothness: (quantity: HourlyWeatherQuantity, value: Float) -> Unit,
    onSaveCurvesSmoothness: (quantity: HourlyWeatherQuantity, value: Float) -> Unit,
    onAddRemoveQuantity: (quantity: HourlyWeatherQuantity, addRemove: Boolean) -> Unit,
    onUpdateNotificationCurrentQuantities: (quantity: HourlyWeatherQuantity, addRemove: Boolean) -> Unit,
    onUpdateNotificationHourlyDiagramIconsVisibility: (visible: Boolean) -> Unit,
    onUpdateNotificationHourlyQuantities: (quantity: HourlyWeatherQuantity, addRemove: Boolean) -> Unit,
    onUpdateNotificationCurveShadowVisibility: (visible: Boolean) -> Unit,
    onSaveNotificationCurvesColor: (curveIndex: Int, color: Color) -> Unit,
    onUpdateWholeNotificationCurvesColor: (colors: List<Color>) -> Unit,
    onUpdateNotificationTimeAxisDialogVisibility: (visible: Boolean) -> Unit,
    onUpdateNotificationTimeAxis: (time: HourlyDiagramTime) -> Unit,

    onUpdateNotificationDailyWeatherIconsVisible: (visible: Boolean) -> Unit,
    onUpdateNotificationDailyQuantity: (quantity: DailyWeatherQuantity) -> Unit,
    onUpdateDailyNotificationGridDialogVisibility: (visible: Boolean) -> Unit,
    onUpdateDailyNotificationChartGrid: (grid: ChartGrids) -> Unit,
    onUpdateLocationDialogVisibility: (visible: Boolean) -> Unit,
    onUpdateNotificationWeatherIconVisibility: (visible: Boolean) -> Unit,
    onUpdateNotificationDateTimeDialogVisibility: (visible: Boolean) -> Unit,
    onUpdateNotificationDateTime: (time: HourlyDiagramTime) -> Unit,
    onUpdateNotificationLocationDialogVisibility: (visible: Boolean) -> Unit,
    onUpdateNotificationLocationId: (id: Int) -> Unit,

    onNavigateUp: () -> Unit
) {
    when {
        settingsState.themeDialogVisibility -> {
            ChooseThemeDialog(
                appTheme = appSettings.theme,
                onDismiss = {
                    onUpdateThemeDialogVisibility(false)
                },
                onConfirmation = {
                    onUpdateTheme(it)
//                    onUpdateThemeDialogVisibility(false)
                }
            )
        }
        settingsState.timeAxisDialogVisibility -> {
            ChooseTimeAxisDialog(
                time = appSettings.hourlyDiagramTime,
                onDismiss = {
                    onUpdateTimeAxisDialogVisibility(false)
                },
                onConfirmation = {
                    onUpdateTimeAxis(it)
//                    onUpdateTimeAxisDialogVisibility(false)
                }
            )
        }
        settingsState.diagramThemeDialogVisibility -> {
            ChooseDiagramThemeDialog(
                chartTheme = hourlyChartState.chartTheme,
                onConfirmation = {
                    onUpdateDiagramThemeDialogVisibility(false)
                },
                onSelectTheme = {
                    onUpdateChartTheme(it)
                }
            )
        }
        settingsState.diagramGridDialogVisibility -> {
            ChooseDiagramGridDialog(
                chartGrid = hourlyChartState.chartGrid,
                onDismiss = {
                    onUpdateGridDialogVisibility(false)
                },
                onConfirmation = {
                    onUpdateChartGrid(it)
                },
            )
        }
        settingsState.notificationTimeAxisDialogVisibility -> {
            ChooseTimeAxisDialog(
                time = appSettings.notificationHourlyDiagramTime,
                onDismiss = {
                    onUpdateNotificationTimeAxisDialogVisibility(false)
                },
                onConfirmation = {
                    onUpdateNotificationTimeAxis(it)
                }
            )
        }
        settingsState.notificationDiagramGridDialogVisibility -> {
            ChooseDiagramGridDialog(
                chartGrid = appSettings.notificationDailyChartGrid,
                onDismiss = {
                    onUpdateDailyNotificationGridDialogVisibility(false)
                },
                onConfirmation = {
                    onUpdateDailyNotificationChartGrid(it)
                },
            )
        }
        settingsState.notificationDateTimeDialogVisibility -> {
            ChooseTimeAxisDialog(
                time = appSettings.notificationDateTime,
                title = "Date and time",
                onDismiss = {
                    onUpdateNotificationDateTimeDialogVisibility(false)
                },
                onConfirmation = {
                    onUpdateNotificationDateTime(it)
                }
            )
        }
        settingsState.notificationLocationDialogVisibility -> {
            ChooseLocationDialog(
                locationList = allLocations,
                locationId = appSettings.notificationLocationId,
                onDismiss = {
                    onUpdateNotificationLocationDialogVisibility(false)
                },
                onConfirmation = {
                    onUpdateNotificationLocationId(it)
                }
            )
        }
    }


    val contentTransform = when {
        settingsState.paneContent == SettingsPaneContent.MAIN ||
                (settingsState.paneContent == SettingsPaneContent.GENERAL &&
                        (settingsState.previousPaneContent == SettingsPaneContent.CARD_WEATHER_QUANTITY ||
                                settingsState.previousPaneContent == SettingsPaneContent.HOURLY_DIAGRAM)) -> {
            ContentTransform(
                targetContentEnter = /*slideInHorizontally(
                initialOffsetX = { (-0.6f * it).toInt() }
            )*/ scaleIn(initialScale = 0.8f) + fadeIn(initialAlpha = 0f),
                initialContentExit = slideOutHorizontally(
                    targetOffsetX = { (0.6f * it).toInt() }
                ) + fadeOut()
            )
        }
        else -> {
            ContentTransform(
                targetContentEnter = slideInHorizontally(
//                animationSpec = tween(durationMillis = 1000),
                    initialOffsetX = { (0.6f * it).toInt() }
                ) + fadeIn(),
                initialContentExit = /*slideOutHorizontally(
                targetOffsetX = { (-0.6f * it).toInt() }
            )*/ scaleOut(targetScale = 0.8f) + fadeOut(targetAlpha = 0f)
            )
        }
    }
    AnimatedContent(
        modifier = Modifier.fillMaxSize(),
        targetState = settingsState.paneContent,
        label = "donateContent",
        transitionSpec = {
            contentTransform
        }
    ) { settingsContent ->
        when (settingsContent) {
            SettingsPaneContent.MAIN -> {
                MainSettingsScreen(
                    modifier = modifier,
                    appSettings = appSettings,
                    onUpdateNotificationVisibility = onUpdateNotificationVisibility,
                    onNavigateUp = onNavigateUp,
                    onUpdatePaneContent = onUpdatePaneContent
                )
            }
            SettingsPaneContent.GENERAL -> {
                GeneralSettingsScreen(
                    modifier = modifier,
                    appSettings = appSettings,
                    singlePane = singlePane,
                    onUpdateSilence = onUpdateSilence,
                    onUpdateVibration = onUpdateVibration,
                    onUpdateScreenOn = onUpdateScreenOn,
                    onUpdateThemeDialogVisibility = onUpdateThemeDialogVisibility,
                    onUpdateDynamicTheme = onUpdateDynamicTheme,
                    onUpdateClockGaugeVisibility = onUpdateClockGaugeVisibility,
//                    onUpdateHomeCardDialogVisibility = onUpdateHomeCardDialogVisibility,
                    onUpdatePaneContent = onUpdatePaneContent,
                    onNavigateUp = {
                        onUpdatePaneContent(SettingsPaneContent.MAIN, SettingsPaneContent.GENERAL)
                    },
                )
            }
            SettingsPaneContent.UNIT -> {
                UnitSettingsScreen(
                    modifier = modifier,
                    appSettings = appSettings,
                    singlePane = singlePane,
                    onUpdateLengthUnit = onUpdateLengthUnit,
                    onUpdateTemperatureUnit = onUpdateTemperatureUnit,
                    onUpdatePressureUnit = onUpdatePressureUnit,
                    onUpdateSpeedUnit = onUpdateSpeedUnit,
                    onNavigateUp = {
                        onUpdatePaneContent(SettingsPaneContent.MAIN, SettingsPaneContent.UNIT)
                    }
                )
            }
            SettingsPaneContent.NOTIFICATION -> {
                NotificationSettingsScreen(
                    modifier = modifier,
                    appSettings = appSettings,
                    singlePane = singlePane,
                    notificationHourlyChartData = notificationHourlyChartData,
                    notificationDailyChartData = notificationDailyChartData,
                    onUpdateNotificationVisibility = onUpdateNotificationVisibility,
                    onUpdateNotificationCurrentQuantities = onUpdateNotificationCurrentQuantities,
                    onUpdateNotificationHourlyDiagramIconsVisibility =
                        onUpdateNotificationHourlyDiagramIconsVisibility,
                    onUpdateNotificationHourlyQuantities = onUpdateNotificationHourlyQuantities,
                    onUpdateNotificationCurveShadowVisibility = onUpdateNotificationCurveShadowVisibility,
                    onSaveCurvesColor = onSaveNotificationCurvesColor,
                    onUpdateWholeCurvesColor = onUpdateWholeNotificationCurvesColor,
                    onUpdateNotificationTimeAxisDialogVisibility = onUpdateNotificationTimeAxisDialogVisibility,
                    onUpdateNotificationDailyWeatherIconsVisible = onUpdateNotificationDailyWeatherIconsVisible,
                    onUpdateNotificationDailyQuantity = onUpdateNotificationDailyQuantity,
                    onUpdateGridDialogVisibility = onUpdateDailyNotificationGridDialogVisibility,
                    onUpdateLocationDialogVisibility = onUpdateLocationDialogVisibility,
                    onUpdateNotificationWeatherIconVisibility = onUpdateNotificationWeatherIconVisibility,
                    onUpdateNotificationDateTimeDialogVisibility = onUpdateNotificationDateTimeDialogVisibility,

                    onNavigateUp = {
                        onUpdatePaneContent(
                            SettingsPaneContent.MAIN,
                            SettingsPaneContent.NOTIFICATION
                        )
                    },

                )
            }
            SettingsPaneContent.WIDGET -> {}
            SettingsPaneContent.CARD_WEATHER_QUANTITY -> {
                HomeCardWeatherQuantityScreen(
                    modifier = modifier,
                    itemState = itemState,
                    appSettings = appSettings,
                    onUpdateHomeCardQuantityUnitsVisibility = onUpdateHomeCardQuantityUnitsVisibility,
                    cardWeatherQuantities = appSettings.cardWeatherQuantities,
                    onUpdateCardWeatherQuantities = onUpdateCardWeatherQuantities,
                    onNavigateUp = {
                        onUpdatePaneContent(
                            SettingsPaneContent.GENERAL,
                            SettingsPaneContent.CARD_WEATHER_QUANTITY
                        )
                    },
                )
            }
            SettingsPaneContent.HOURLY_DIAGRAM -> {
                HourlyDiagramCurvesScreen(
                    modifier = modifier,
                    appSettings = appSettings,
                    singlePane = singlePane,
                    hourlyChartData = hourlyChartData,
                    hourlyChartState = hourlyChartState,
                    onUpdateInitialXAxisBounds = onUpdateInitialXAxisBounds,
                    onAddYAxis = onAddYAxis,
                    onMoveHourlyDiagramAxis = onMoveHourlyDiagramAxis,
                    onScaleHourlyDiagramAxis = onScaleHourlyDiagramAxis,

                    onCalculateHorizontalBarSeparation = onCalculateHorizontalBarSeparation,
                    onCalculateVerticalBarSeparation = onCalculateVerticalBarSeparation,
                    onUpdateHourlyChartDotsOnCurveVisibility = onUpdateHourlyChartDotsOnCurveVisibility,
                    onUpdateHourlyChartCurveShadowVisibility = onUpdateHourlyChartCurveShadowVisibility,
                    onUpdateHourlyChartWeatherIconVisibility = onUpdateHourlyChartWeatherIconVisibility,
                    onUpdateHourlyChartSunRiseSetIconsVisibility = onUpdateHourlyChartSunRiseSetIconsVisibility,
                    onUpdateTimeAxisDialogVisibility = onUpdateTimeAxisDialogVisibility,
                    onUpdateThemeDialogVisibility = onUpdateDiagramThemeDialogVisibility,
                    onUpdateGridDialogVisibility = onUpdateGridDialogVisibility,
                    onUpdateCurvesColor = onUpdateCurvesColor,
                    onUpdateWholeCurvesColor = onUpdateWholeCurvesColor,
                    onSaveCurvesColor = onSaveCurvesColor,
                    onUpdateCurvesSmoothness = onUpdateCurvesSmoothness,
                    onSaveCurvesSmoothness = onSaveCurvesSmoothness,
                    onAddRemoveQuantity = onAddRemoveQuantity,

                    onNavigateUp = {
                        onUpdatePaneContent(
                            SettingsPaneContent.MAIN,
                            SettingsPaneContent.HOURLY_DIAGRAM
                        )
                    }
                )
            }
        }
    }
    BackHandler(enabled = !drawerOpen) {
        when (settingsState.paneContent) {
            SettingsPaneContent.MAIN -> onNavigateUp()
            SettingsPaneContent.CARD_WEATHER_QUANTITY -> {
                onUpdatePaneContent(SettingsPaneContent.GENERAL, SettingsPaneContent.CARD_WEATHER_QUANTITY)
            }
            SettingsPaneContent.HOURLY_DIAGRAM -> {
                onUpdatePaneContent(SettingsPaneContent.MAIN, SettingsPaneContent.HOURLY_DIAGRAM)
            }
            else -> onUpdatePaneContent(SettingsPaneContent.MAIN, settingsState.paneContent)
        }
    }
}

@Composable
fun SettingsGroupTitle(
    modifier: Modifier = Modifier,
    title: String,
    icon: (@Composable (() -> Unit))? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth()
            .padding(start = 72.dp, top = 8.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .height(48.dp).wrapContentHeight(),
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        icon?.let {
            Spacer(Modifier.width(16.dp).weight(1f))
            icon()
        }
    }

}

@Composable
fun SettingsRow1(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    title: String? = null,
    titleAnnotated: AnnotatedString? = null,
    subTitle: String? = null,
    dividerVisible: Boolean = false,
    onClick: () -> Unit,
    content: @Composable (() -> Unit)
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ){
        if (icon != null) {
            Icon(
                modifier = Modifier.size(48.dp).padding(12.dp),
                painter = painterResource(icon),
                contentDescription = null
            )
        } else {
            Spacer(Modifier.size(48.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            if (titleAnnotated != null) {
                Text(
                    text = titleAnnotated,
                    style = MaterialTheme.typography.titleLarge
                )
            } else {
                Text(
                    text = title!!,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            subTitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.customColorScheme.lowEmphasisText,
                    lineHeight = 18.sp
                )
            }
        }
        Spacer(modifier = Modifier.width(16.dp).weight(1f))
        if (dividerVisible) {
            Spacer(
                modifier = Modifier
                    .height(32.dp).width(1.dp)
                    .background(color = MaterialTheme.customColorScheme.paneSpacerColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsRow2(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    title: String? = null,
    titleAnnotated: AnnotatedString? = null,
    subTitle: String? = null,
    content: @Composable (() -> Unit)
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 16.dp)
    ) {
        Row {
            Icon(
                modifier = Modifier
                    .size(48.dp)
                    .padding(12.dp),
                painter = painterResource(icon),
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                if (titleAnnotated != null) {
                    Text(
                        text = titleAnnotated,
                        style = MaterialTheme.typography.titleLarge
                    )
                } else {
                    Text(
                        text = title!!,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                subTitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.customColorScheme.lowEmphasisText
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End
        ) {
            content()
        }
    }
}

@Composable
fun SettingsBox(
    modifier: Modifier = Modifier,
    title: String,
    onClick: () -> Unit,
    content: @Composable (() -> Unit)
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(size = 24.dp))
            .clickable(onClick = onClick)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(size = 24.dp)
            ),
        verticalAlignment = Alignment.CenterVertically
    ){
        Text(
            modifier = Modifier.padding(24.dp),
            text = title,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.width(16.dp).weight(1f))
        content()
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsRow3(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    title: String? = null,
    titleAnnotated: AnnotatedString? = null,
    color: Color = Color.Unspecified,
    onClick: () -> Unit,
    subContent: @Composable (() -> Unit),
    content: @Composable (() -> Unit) = {}
) {
    Row(
        modifier = modifier.fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 16.dp)
    ){
        Icon(
            modifier = Modifier.size(48.dp).padding(12.dp),
            painter = painterResource(icon),
            contentDescription = null
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            if (titleAnnotated != null) {
                Text(
                    modifier = Modifier.heightIn(max = 24.dp)
                        .wrapContentHeight(unbounded = true),
                    text = titleAnnotated,
                    style = MaterialTheme.typography.titleLarge,
                    color = color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Text(
                    modifier = Modifier.heightIn(max = 24.dp)
                        .wrapContentHeight(),
                    text = title!!,
                    style = MaterialTheme.typography.titleLarge,
                    color = color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }


            subContent()
        }
        Row(
            modifier = Modifier
                .width(64.dp)
                .fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))
            content()
        }

    }
}

//@Preview(showBackground = true)
//@Composable
//fun SettingsScreenPreview() {
//    WeathererTheme {
//        SettingsScreen(
//            appSettings = AppSettings(),
//            settingsState = SettingsState(),
//            singlePane = false,
//            onUpdateSilence = {},
//            onNavigateUp = {},
//            onUpdateThemeDialogVisibility = {},
//            onUpdateTheme = { _ -> },
//            onUpdateTemperatureUnit = {},
//            onUpdateLengthUnit = {},
//            onUpdatePressureUnit = {},
//            onUpdateSpeedUnit = {},
//            onUpdateScreenOn = {},
//            onUpdateClockGaugeVisibility = {},
//            onUpdateNotificationVisibility = {}
//        )
//    }
//}

@Preview(showBackground = true)
@Composable
fun SettingsGroupTitlePreview() {
    WeathererTheme {
        SettingsGroupTitle(
            modifier = Modifier.fillMaxWidth(),
            title = "Curves Smoothness"
        )
    }
}