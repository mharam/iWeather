package com.takaapoo.weatherer.utility

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities

object NetworkUtils {

    fun isConnectedToInternet(context: Context?): Boolean {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val network: Network? = connectivityManager?.activeNetwork
        val capabilities = connectivityManager?.getNetworkCapabilities(network) ?: return false

        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    fun hasInternetAccess(context: Context): Boolean {
        return if (isConnectedToInternet(context)) {
            try {
                // Ping Google's DNS server to confirm internet access
                val process = Runtime.getRuntime().exec("ping -c 1 8.8.8.8")
                val exitCode = process.waitFor()
                exitCode == 0
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        } else {
            false
        }
    }
}