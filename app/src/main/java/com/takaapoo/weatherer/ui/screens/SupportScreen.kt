package com.takaapoo.weatherer.ui.screens

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.google.android.play.core.review.ReviewManagerFactory
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.ui.theme.Gray20
import com.takaapoo.weatherer.ui.theme.Transparent
import com.takaapoo.weatherer.ui.theme.customColorScheme
import com.takaapoo.weatherer.ui.theme.gerdFontFamily

@Composable
fun SupportScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
) {
    val density = LocalDensity.current
    val activity = LocalActivity.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer),
                title = {
                    Text(
                        text = "Support iWeather",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {

                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Transparent
                ),
                windowInsets = WindowInsets(
                    left = 0, top = TopAppBarDefaults.windowInsets.getTop(density), right = 0, bottom = 0
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 500.dp)
                .background(color = MaterialTheme.customColorScheme.detailScreenSurface)
                .padding(
                    top = paddingValues.calculateTopPadding(),
                )
                .verticalScroll(rememberScrollState()),
        ) {
//            Spacer(modifier = Modifier.height(8.dp))
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(332.dp)
                    .align(Alignment.CenterHorizontally),
                painter = painterResource(id = R.drawable.support_art),
                contentDescription = null,
                contentScale = ContentScale.FillHeight,
            )
            Spacer(modifier = Modifier.height(32.dp))
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.support_us_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = gerdFontFamily
                )
                Spacer(Modifier.height(48.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .padding(horizontal = 16.dp)
                        .background(Gray20)
                )
                Spacer(Modifier.height(48.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Spacer(Modifier.weight(1f))
                    FilledIconButton(
                        onClick = {
                            activity?.let {
                                rateApp(
                                    activity = it,
                                    comeFromRateButton = true
                                )
                            }
                        },
                        content = {
                            Icon(
                                painter = painterResource(R.drawable.star_rate_24px),
                                contentDescription = "Rate",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    )
                    FilledIconButton(
                        onClick = {
                            activity?.let { shareApp(it) }
                        },
                        content = {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    )
                    Spacer(Modifier.weight(1f))
                }






                /*OutlinedButton(  // Rate button
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                    onClick = {
                        activity?.let {
                            rateApp(
                                activity = it,
                                comeFromRateButton = true
                            )
                        }
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painterResource(R.drawable.star_rate_24px),
                            contentDescription = "Rate",
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                        Text(stringResource(R.string.rate_iWeather))
                    }
                }
                Spacer(Modifier.height(16.dp))
                OutlinedButton(  // Share button
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                    onClick = {
                        activity?.let { shareApp(it) }
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Share",
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                        Text(stringResource(R.string.share))
                    }
                }*/
            }
        }
    }
}


private fun shareApp(
    activity: Activity,
){
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        flags = Intent.FLAG_ACTIVITY_NO_HISTORY
//                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
//                Intent.FLAG_ACTIVITY_MULTIPLE_TASK

        putExtra(Intent.EXTRA_SUBJECT, activity.resources.getString(R.string.introduce_app_subj))
        putExtra(Intent.EXTRA_TEXT, activity.resources.getString(R.string.introduce_app_text))
    }
    activity.startActivity(Intent.createChooser(intent, "share via"))
}

fun rateApp(activity: Activity, comeFromRateButton: Boolean){
    val uri = "market://details?id=${activity.packageName}".toUri()
    val globalIntent = Intent(Intent.ACTION_VIEW, uri).apply {
        flags = Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK
    }

    if (comeFromRateButton){
        if (globalIntent.resolveActivity(activity.packageManager) != null) {
            activity.startActivity(globalIntent)
        } else {
            activity.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    "http://play.google.com/store/apps/details?id=${activity.packageName}".toUri()
                )
            )
        }
    } else {
        val manager = ReviewManagerFactory.create(activity)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // We got the ReviewInfo object
                val flow = task.result?.let { manager.launchReviewFlow(activity, it) }
                flow?.addOnCompleteListener {
                    Log.i("review", "flow is complete")
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown. Thus, no
                    // matter the result, we continue our app flow.
                }
            } else {
                // There was some problem, log or handle the error code.
//                        @ReviewErrorCode val reviewErrorCode = (task.exception as RuntimeExecutionException).errorCode
//                        Timber.i("reviewErrorCode = $reviewErrorCode")
            }
        }
    }

}

