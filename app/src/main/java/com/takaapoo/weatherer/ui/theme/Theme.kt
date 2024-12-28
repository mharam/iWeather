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
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.takaapoo.weatherer.domain.model.AppTheme
import com.takaapoo.weatherer.ui.viewModels.PreferenceViewModel

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
)

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    surface = Purple80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun WeathererTheme(
    systemDarkTheme: Boolean = isSystemInDarkTheme(),
    preferenceViewModel: PreferenceViewModel = hiltViewModel(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val appSettings by preferenceViewModel.appSettings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val darkTheme = (systemDarkTheme && appSettings.theme != AppTheme.LIGHT) ||
            appSettings.theme == AppTheme.DARK

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val customColorScheme = if (darkTheme) DarkCustomColorScheme else LightCustomColorScheme

    val view = LocalView.current
    val window = (context as Activity).window
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
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
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