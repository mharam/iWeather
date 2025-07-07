package com.takaapoo.weatherer.ui.screens.settings

import androidx.compose.ui.graphics.Color
import com.takaapoo.weatherer.ui.theme.curveBlue
import com.takaapoo.weatherer.ui.theme.curveGreen
import com.takaapoo.weatherer.ui.theme.curveOrange
import com.takaapoo.weatherer.ui.theme.curvePink

data class HourlyCurveColor(
    val setId: Int = 1,
    val colors: List<Color> = listOf(curveBlue, curvePink, curveGreen, curveOrange)
)

val predefinedHourlyCurveColor = listOf(
    HourlyCurveColor(),
    HourlyCurveColor(
        setId = 2,
        colors = listOf(Color(0xFF0288D1), Color(0xFF7B1FA2), Color(0xFF2C9B31), Color(0xFFE34343))
    ),
    HourlyCurveColor(
        setId = 3,
        colors = listOf(Color(0xFF1D90F6), Color(0xFF8940EF), Color(0xFF34C5A8), Color(0xFFC55C3D)),
    ),
    HourlyCurveColor(
        setId = 4,
        colors = listOf(Color(0xFF3B9DCB), Color(0xFFF655FF), Color(0xFF18CBB6), Color(0xFFD32D2D)),
    ),
    HourlyCurveColor(
        setId = 5,
        colors = listOf(Color(0xFF388E3C), Color(0xFFE91E63), Color(0xFF9CCC65), Color(0xFFFFA726)),
    ),
    HourlyCurveColor(
        setId = 6,
        colors = listOf(Color(0xFF3D5AFE), Color(0xFFD500F9), Color(0xFF00F504), Color(0xFFFF6D00)),
    ),
    HourlyCurveColor(
        setId = 7,
        colors = listOf(Color(0xFFAA00FF), Color(0xFF00C853), Color(0xFF1FE6EE), Color(0xFFD50000)),
    ),
    HourlyCurveColor(
        setId = 8,
        colors = listOf(Color(0xFF335FE3), Color(0xFFC34ECB), Color(0xFF64A422), Color(0xFFBD7E4A)),
    ),
)
