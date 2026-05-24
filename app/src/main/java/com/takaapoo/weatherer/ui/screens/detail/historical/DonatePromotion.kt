package com.takaapoo.weatherer.ui.screens.detail.historical

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.ui.theme.gerdFontFamily
import com.takaapoo.weatherer.utility.toDp

@Composable
fun DonatePromotion(
    modifier: Modifier = Modifier,
    columnHeight: Float,
    maxScroll: Float,
    scrollState: ScrollState,
    onNavigateToDonateScreen: () -> Unit,
) {
    val density = LocalDensity.current
    LaunchedEffect(Unit) {
        if (scrollState.value > maxScroll) {
            scrollState.scrollTo(maxScroll.toInt())
        }
    }
    val lottieComposition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(R.raw.lock_unlock_key)
    )

    Column(
        modifier = modifier
            .widthIn(max = 500.dp)
            .heightIn(min = columnHeight.toDp(density))
            .padding(16.dp)
    ) {
        LottieAnimation(
            composition = lottieComposition,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            iterations = LottieConstants.IterateForever
        )
        Text(
            text = stringResource(R.string.history_weather_donate_title),
            style = MaterialTheme.typography.titleMedium,
            fontFamily = gerdFontFamily
        )
        Spacer(Modifier.Companion.height(16.dp))
        Text(
            text = stringResource(R.string.history_weather_donate_motivation),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Start
        )
        Spacer(Modifier.Companion.height(32.dp))

        Button(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            onClick = onNavigateToDonateScreen
        ) {
            Text(
                text = "Donate",
                fontFamily = gerdFontFamily,
                fontSize = 16.sp
            )
        }
    }
}