package com.takaapoo.weatherer.utility

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.os.Process
import android.provider.Settings
import android.util.Log

object BackgroundRestrictionChecker {

    fun isBackgroundRestricted(context: Context): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val restrictedManufacturers = listOf("xiaomi", "huawei", "oneplus", "oppo", "vivo", "samsung")

        return restrictedManufacturers.contains(manufacturer) ||
                isBatteryOptimizationEnabled(context) ||
                areBackgroundRestrictionsEnabled(context)
    }

    private fun isBatteryOptimizationEnabled(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return !powerManager.isIgnoringBatteryOptimizations(context.packageName)
    }

    fun areBackgroundRestrictionsEnabled(context: Context): Boolean {
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode =
            appOpsManager.checkOpNoThrow(
                "android:run_in_background",
                Process.myUid(),
                context.packageName
            )
        Log.i("back1", "mode = $mode")
        return mode == AppOpsManager.MODE_IGNORED || mode == AppOpsManager.MODE_ERRORED
    }

    fun guideUserToDisableRestrictions(context: Context) {
        if (isBackgroundRestricted(context)) {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
            context.startActivity(intent)
        }
    }
}