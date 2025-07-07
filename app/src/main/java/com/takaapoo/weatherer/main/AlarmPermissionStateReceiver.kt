package com.takaapoo.weatherer.main

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.ALARM_SERVICE
import android.content.Intent
import android.os.Build
import android.util.Log

class AlarmPermissionStateReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
//        Log.i("alarm1", "onReceive")
        val alarmManager = context?.getSystemService(ALARM_SERVICE) as? AlarmManager
        val actions = listOf(Intent.ACTION_MY_PACKAGE_REPLACED, Intent.ACTION_PACKAGE_RESTARTED)

        if (actions.contains(intent?.action) && Build.VERSION.SDK_INT >= 31 &&
            alarmManager?.canScheduleExactAlarms() == false){
            Log.i("alarm1", "exact revoked!")
        }

    }
}