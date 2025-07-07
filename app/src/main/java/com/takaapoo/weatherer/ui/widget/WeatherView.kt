package com.takaapoo.weatherer.ui.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import androidx.appcompat.widget.AppCompatImageView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.takaapoo.weatherer.domain.WeatherType
import com.takaapoo.weatherer.moonPhaseMap
import com.takaapoo.weatherer.utility.readJSONFromAssets
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

class WeatherView(
    context: Context,
    val weatherType: WeatherType,
): AppCompatImageView(context) {

    init {
        moonPhaseMap = Json.parseToJsonElement(readJSONFromAssets(context, "moon_phase.json"))
            .jsonObject.toMap()
    }

    val weatherPaint = Paint()
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        WeatherType.drawWeatherIconOnCanvas(
            canvas = canvas,
            weatherType = weatherType,
            context = context,
            topLeft = IntOffset.Zero,
            iconSize = IntSize(width, height),
            paint = weatherPaint,
        )
    }

}