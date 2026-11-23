package com.takaapoo.weatherer.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.ui.theme.Gray20
import com.takaapoo.weatherer.ui.theme.customColorScheme
import com.takaapoo.weatherer.ui.theme.gerdFontFamily
import com.takaapoo.weatherer.utility.getDeviceName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.net.URLEncoder


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactUsScreen(
    modifier: Modifier = Modifier,
    onNavigateUp: () -> Unit,
) {
    val density = LocalDensity.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val appVersionName = (if (Build.VERSION.SDK_INT >= 33)
        context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.PackageInfoFlags.of(0)
        )
    else
        context.packageManager.getPackageInfo(
            context.packageName,
            0
        )
            ).versionName ?: ""


    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
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
                modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer),
                title = {
                    Text(
                        text = "Contact us",
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
                    containerColor = Color(0x00FFFFFF)
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
                    start = 16.dp,
                    end = 16.dp
                )
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Image(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .align(Alignment.CenterHorizontally),
                painter = painterResource(id = R.drawable.mail_box),
                contentDescription = null,
                contentScale = ContentScale.FillHeight,
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.contact_us_title),
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
                        feedback(
                            buttonCode = 1,
                            context = context,
                            snackbarHostState = snackbarHostState,
                            coroutineScope = coroutineScope,
                            appVersionName = appVersionName
                        )
                    },
                    content = {
                        Icon(
                            painter = painterResource(R.drawable.mail_24px),
                            contentDescription = "Email",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                )
                FilledIconButton(
                    onClick = {
                        feedback(
                            buttonCode = 2,
                            context = context,
                            snackbarHostState = snackbarHostState,
                            coroutineScope = coroutineScope,
                            appVersionName = appVersionName
                        )
                    },
                    content = {
                        Icon(
                            painter = painterResource(R.drawable.telegram),
                            contentDescription = "Telegram",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                )
                FilledIconButton(
                    onClick = {
                        feedback(
                            buttonCode = 3,
                            context = context,
                            snackbarHostState = snackbarHostState,
                            coroutineScope = coroutineScope,
                            appVersionName = appVersionName
                        )
                    },
                    content = {
                        Icon(
                            painter = painterResource(R.drawable.whatsapp),
                            contentDescription = "whatsApp",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                )
                Spacer(Modifier.weight(1f))
            }

        }
    }
}



fun feedback(
    buttonCode: Int,
    context: Context,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
    appVersionName: String,
) {
    when (buttonCode) {
        1 -> openEmailApp(context, appVersionName, snackbarHostState, coroutineScope)
        2 -> openTelegram(context, appVersionName, snackbarHostState, coroutineScope)
        3 -> openWhatsApp(context, appVersionName, snackbarHostState, coroutineScope)
    }
}

private fun openEmailApp(
    context: Context,
    appVersionName: String,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
) {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = "mailto:".toUri()
        putExtra(Intent.EXTRA_EMAIL, arrayOf("support@takaapoo.com"))
        putExtra(Intent.EXTRA_SUBJECT, "iWeather ($appVersionName)")
        putExtra(Intent.EXTRA_TEXT,
            context.resources.getString(
                R.string.app_name_and_version,
                appVersionName, Build.VERSION.SDK_INT.toString(), getDeviceName()
            )
        )
    }

    try {
        context.startActivity(Intent.createChooser(intent, "Choose Email App"))
    } catch (_: ActivityNotFoundException) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar("No email app is installed!")
        }
    }
}

private fun openTelegram(
    context: Context,
    appVersionName: String,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
) {
    val uri = "tg://resolve?domain=mharam87".toUri()
    val telegramIntent = Intent(Intent.ACTION_VIEW, uri).apply {
        putExtra(Intent.EXTRA_TEXT,
            context.resources.getString(
                R.string.app_name_and_version,
                appVersionName, Build.VERSION.SDK_INT.toString(), getDeviceName()
            )
        )
    }

    try {
        context.startActivity(telegramIntent)
    } catch (_: ActivityNotFoundException) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar("Telegram is not installed!")
        }
    }
}

private fun openWhatsApp(
    context: Context,
    appVersionName: String,
    snackbarHostState: SnackbarHostState,
    coroutineScope: CoroutineScope,
) {
    val phone = "989127790828"
    val message = URLEncoder.encode(
        context.resources.getString(
            R.string.app_name_and_version,
            appVersionName, Build.VERSION.SDK_INT.toString(), getDeviceName()
        ),
        "UTF-8"
    )
    val uri = "https://wa.me/$phone?text=$message".toUri()
    val whatsappIntent = Intent(Intent.ACTION_VIEW, uri).apply {
//        `package` = "com.whatsapp"
    }

    try {
        context.startActivity(whatsappIntent)
    } catch (_: ActivityNotFoundException) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar("WhatsApp is not installed!")
        }
    }
}



private fun isPackageInstalled(
    packageManager: PackageManager,
    packageName: String
): Boolean {
    return try {
        packageManager.getPackageInfo(packageName, 0)
        true
    } catch (_: PackageManager.NameNotFoundException) {
        false
    }
}