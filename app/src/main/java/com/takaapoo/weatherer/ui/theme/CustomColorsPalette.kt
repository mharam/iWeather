package com.takaapoo.weatherer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class CustomColorsPalette(
    val searchbarSurface: Color = Color.Unspecified,
    val statusBarScrimColor: Color = Color.Unspecified,
    val mapFabRed: Color = Color.Unspecified,
    val mapFabBlue: Color = Color.Unspecified,
    val cardBorder: Color = Color.Unspecified,
    val noGridIcon: Color = Color.Unspecified,
    val detailScreenSurface: Color = Color.Unspecified,
    val onDetailScreenSurface: Color = Color.Unspecified,
    val lowEmphasisText: Color = Color.Unspecified,
    val minTemperature: Color = Color.Unspecified,
    val railBackground: Color = Color.Unspecified,
    val appThemeDiagramSurfaceColor: Color = Color.Unspecified,
    val appThemeDiagramOnSurfaceColor: Color = Color.Unspecified,
)

val LocalCustomColorsPalette = staticCompositionLocalOf { CustomColorsPalette() }
val MaterialTheme.customColorScheme: CustomColorsPalette
    @Composable
    @ReadOnlyComposable
    get() = LocalCustomColorsPalette.current