package com.takaapoo.weatherer.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.AppTheme

private val DarkCustomColorScheme = CustomColorsPalette(
    searchbarSurface = Gray50,
    statusBarScrimColor = StatusBarDarkScrim,
    mapFabRed = fabLightRed,
    mapFabBlue = fabLightBlue,
    cardBorder = Green80,
    noGridIcon = Gray40,
    detailScreenSurface = DetailScreenDarkSurface,
    lowEmphasisText = Gray20,
    minTemperature = BarBlueLight,
    railBackground = RailDarkBackground,
    onDetailScreenSurface = onDetailScreenDarkSurface,
    appThemeDiagramSurfaceColor = DiagramDarkTheme,
    appThemeDiagramOnSurfaceColor = OnDiagramDarkTheme,
    paneSpacerColor = Gray40,
)
private val LightCustomColorScheme = CustomColorsPalette(
    searchbarSurface = Color.White,
    statusBarScrimColor = StatusBarLightScrim,
    mapFabRed = fabDarkRed,
    mapFabBlue = fabDarkBlue,
    cardBorder = Green40,
    noGridIcon = Gray10,
    detailScreenSurface = DetailScreenLightSurface,
    lowEmphasisText = Gray50,
    minTemperature = BarBlue,
    railBackground = RailLightBackground,
    onDetailScreenSurface = onDetailScreenLightSurface,
    appThemeDiagramSurfaceColor = DiagramLightTheme,
    appThemeDiagramOnSurfaceColor = OnDiagramLightTheme,
    paneSpacerColor = Gray20,
)

val DarkColorScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark
)

val LightColorScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,

    )

@Composable
fun WeathererTheme(
    systemDarkTheme: Boolean = isSystemInDarkTheme(),
//    preferenceViewModel: PreferenceViewModel = hiltViewModel(),
    appSettings: AppSettings = AppSettings(),
    // Dynamic color is available on Android 12+
//    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
//    val appSettings by preferenceViewModel.appSettings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val darkTheme = (systemDarkTheme && appSettings.theme != AppTheme.LIGHT) ||
            appSettings.theme == AppTheme.DARK

    val colorScheme = when {
        appSettings.dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val customColorScheme = if (darkTheme) DarkCustomColorScheme else LightCustomColorScheme

    val view = LocalView.current

//    if (!view.isInEditMode) {
//        SideEffect {
//            window.statusBarColor = /*colorScheme.primary.toArgb()*/ Transparent.toArgb()
//            window.navigationBarColor = Transparent99.toArgb()
//            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
//            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
//        }
//    }
    if (!view.isInEditMode) {
//        WindowCompat.setDecorFitsSystemWindows(window, false)
        (context as? Activity)?.window?.let {
            WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(it, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalCustomColorsPalette provides customColorScheme
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}