package com.example.foodrecommendationapps.api

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest

object NetworkConnectivityChecker {
    lateinit var connectivityManager: ConnectivityManager
    fun isClientConnected(context: Context): Boolean {
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
    fun getNetworkType(context: Context): NetworkType {
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return NetworkType.NONE
        val capabilities = connectivityManager.getNetworkCapabilities(network)
            ?: return NetworkType.NONE
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
            else -> NetworkType.OTHER
        }
    }
    suspend fun isServerAccessible(context: Context): ServerStatus {
        if (!isClientConnected(context)) {
            return ServerStatus.NoInternet
        }
        return try {
            val api = RetrofitBuilder.build().create(HealthService::class.java)
            val response = api.healthCheck()

            when {
                response.isSuccessful -> ServerStatus.Accessible
                response.code() in 500..599 -> ServerStatus.ServerError(response.code())
                else -> ServerStatus.ClientError(response.code())
            }
        } catch (e: Exception) {
            ServerStatus.Unreachable(e.message ?: "Unknown error")
        }
    }
    fun registerNetworkCallback(callback: NetworkCallback, context: Context) {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                callback.onNetworkAvailable()
            }

            override fun onLost(network: Network) {
                callback.onNetworkLost()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                capabilities: NetworkCapabilities
            ) {
                val hasInternet = capabilities
                    .hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                val isValidated = capabilities
                    .hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)

                if (hasInternet && isValidated) {
                    callback.onNetworkAvailable()
                }
            }
        }
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }
    fun unregisterNetworkCallback(networkCallback: ConnectivityManager.NetworkCallback, context: Context) {
        try {
            connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (e: Exception) {
            // Callback was already unregistered or never registered
        }
    }

    enum class NetworkType {
        WIFI, CELLULAR, ETHERNET, OTHER, NONE
    }
    sealed class ServerStatus {
        object Accessible : ServerStatus()
        object NoInternet : ServerStatus()
        data class ServerError(val code: Int) : ServerStatus()
        data class ClientError(val code: Int) : ServerStatus()
        data class Unreachable(val message: String) : ServerStatus()
    }
    interface NetworkCallback {
        fun onNetworkAvailable()
        fun onNetworkLost()
    }
}