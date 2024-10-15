package com.iti.itp.bazaar.network

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network

object NetworkUtil {
    private var isNetworkAvailable: Boolean = false

    fun initialize(context: Context) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isNetworkAvailable = true
            }

            override fun onLost(network: Network) {
                isNetworkAvailable = false
            }
        }
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    fun isNetworkAvailable(): Boolean {
        return isNetworkAvailable
    }
}

class MyApplication : Application() {
    companion object {
        lateinit var instance: MyApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        NetworkUtil.initialize(this)
    }
}
