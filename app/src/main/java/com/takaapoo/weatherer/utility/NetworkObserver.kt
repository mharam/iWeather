package com.takaapoo.weatherer.utility

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class NetworkObserver(context: Context) {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            as ConnectivityManager
    fun isConnectedToInternet(network: Network? = null): Boolean {
        val activeNetwork: Network = (network ?: connectivityManager.activeNetwork) ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
    }

    suspend fun hasInternetAccess(network: Network? = null): Boolean = withContext(Dispatchers.IO) {
        if (!isConnectedToInternet(network)) return@withContext false
        
        repeat(3) { attempt ->
            try {
                val socket = network?.socketFactory?.createSocket() ?: Socket()
                socket.connect(InetSocketAddress("takaapoo.com", 80), 1500)
                socket.close()
                return@withContext true
            } catch (_: Exception) {
                if (attempt < 2) delay(100.milliseconds) // Small delay before retry
            }
        }
        false
    }

    // A callbackFlow that emits network status changes
    val connectivityStatusFlow = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                launch { send(hasInternetAccess(network)) }
            }
            override fun onLost(network: Network) {
                launch { send(hasInternetAccess(network)) }
            }
            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                launch { send(hasInternetAccess(network)) }
            }
        }

//        val networkRequest = NetworkRequest.Builder()
//            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
//            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
//            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
//            .build()
        connectivityManager.registerDefaultNetworkCallback(callback)

        // Send the initial state upon subscription
        launch(Dispatchers.IO) {
            do {
                send(hasInternetAccess())
                delay(10.seconds)
            } while (true)
        }
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
}