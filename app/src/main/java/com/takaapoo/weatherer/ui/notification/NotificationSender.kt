package com.takaapoo.weatherer.ui.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.view.View
import android.view.WindowManager
import android.widget.RemoteViews
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.takaapoo.weatherer.MainActivity
import com.takaapoo.weatherer.R
import com.takaapoo.weatherer.WEATHER_CHANNEL_ID
import com.takaapoo.weatherer.WEATHER_NOTIFICATION_ID
import com.takaapoo.weatherer.data.local.LocalDailyWeather
import com.takaapoo.weatherer.domain.model.AppSettings
import com.takaapoo.weatherer.domain.model.HourlyChartDto
import com.takaapoo.weatherer.ui.notification.custom_views.StatusBarIcon
import com.takaapoo.weatherer.ui.notification.remote_layouts.Header1Layout
import com.takaapoo.weatherer.ui.notification.remote_layouts.Header2Layout
import com.takaapoo.weatherer.ui.notification.remote_layouts.Header3Layout
import com.takaapoo.weatherer.ui.notification.remote_layouts.SmallLayout
import com.takaapoo.weatherer.ui.screens.detail.HourlyWeatherQuantity
import com.takaapoo.weatherer.utility.getNotificationWidth
import com.takaapoo.weatherer.utility.getWindowSize
import com.takaapoo.weatherer.utility.removeTransparentBorder
import com.takaapoo.weatherer.utility.renderViewToBitmap
import com.takaapoo.weatherer.utility.toDp
import com.takaapoo.weatherer.utility.toPx

class NotificationSender(
    val context: Context,
    val header: Int,
    val notificationHourlyChartData: List<HourlyChartDto>,
    val notificationDailyChartData: List<LocalDailyWeather>,
    val appSettings: AppSettings,
    val hourlyChartQuantities: List<HourlyWeatherQuantity>,
    val currentQuantities: List<HourlyWeatherQuantity>,
) {
    val notificationManager = NotificationManagerCompat.from(context)
    val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val windowSize = getWindowSize(windowManager)
    val density = Density(context)
    val notificationWidth = getNotificationWidth(
        windowSize.width.toDp(density),
        windowSize.height.toDp(density)
    )

    val largeRemoteView = RemoteViews(context.packageName, R.layout.notification_large2)
    val smallRemoteView = RemoteViews(context.packageName, R.layout.notification_small)

    init {
        prepareSmallLayout()
        prepareLargeLayout(header)
    }

    private fun prepareSmallLayout(){
        SmallLayout(
            smallRemoteView,
            context,
            notificationHourlyChartData,
            notificationDailyChartData,
            appSettings
        )
    }

    private fun prepareLargeLayout(header: Int){
        when (header){
            1 -> Header1Layout(
                largeRemoteView,
                notificationWidth,
                context,
                notificationHourlyChartData,
                appSettings,
                currentQuantities,
            )
            2 -> Header2Layout(
                largeRemoteView,
                notificationWidth,
                context,
                notificationHourlyChartData,
                appSettings,
                hourlyChartQuantities,
            )
            else -> Header3Layout(
                largeRemoteView,
                notificationWidth,
                context,
                notificationDailyChartData,
                appSettings,
            )
        }
        largeRemoteView.apply {
            setOnClickPendingIntent(R.id.header1, headerPendingIntent(context, 1))
            setOnClickPendingIntent(R.id.header2, headerPendingIntent(context, 2))
            setOnClickPendingIntent(R.id.header3, headerPendingIntent(context, 3))
        }
    }

    val statusBarIcon = StatusBarIcon(
        context = context,
        hourlyChartData = notificationHourlyChartData,
        quantity = HourlyWeatherQuantity.TEMPERATURE,
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
    }

    fun sendNotification(){
        val intent = Intent(context, MainActivity::class.java).apply {
//            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val activityIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val temperatureBitmap = renderViewToBitmap(statusBarIcon)
            ?.removeTransparentBorder(n = 3, minHeight = 38.dp.toPx(density).toInt())
        val icon = IconCompat.createWithBitmap(temperatureBitmap!!)

        val notification = NotificationCompat.Builder(context, WEATHER_CHANNEL_ID)
            .setSmallIcon(icon)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(smallRemoteView)
            .setCustomBigContentView(largeRemoteView)
            .setContentIntent(activityIntent)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setSilent(true)
            .build()

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return
        notificationManager.notify(WEATHER_NOTIFICATION_ID, notification)
    }



    companion object {
        fun headerPendingIntent(context: Context, header: Int): PendingIntent {
            val changeHeaderIntent = Intent(context, NotificationHeaderReceiver::class.java).apply {
                putExtra("header", header)
            }
            return PendingIntent.getBroadcast(
                context,
                header,
                changeHeaderIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }


}