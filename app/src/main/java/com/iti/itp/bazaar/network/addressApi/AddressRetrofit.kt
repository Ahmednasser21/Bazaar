package com.iti.itp.bazaar.network.addressApi

import android.util.Base64
import com.iti.itp.bazaar.network.MyApplication
import com.iti.itp.bazaar.network.NetworkUtil
import okhttp3.Cache
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File


object AddressRetrofit {
    private const val BASE_URL = "https://itp-ism-and2.myshopify.com/"
    private const val CACHE_SIZE = 10 * 1024 * 1024 // 10 MB
    private const val API_KEY = "11e78826cf84e3b78e84a8a635e8c91e"
    private const val ACCESS_TOKEN = "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"

    private val onlineInterceptor: Interceptor = Interceptor { chain ->
        val response = chain.proceed(chain.request())
        val maxAge = 60
        response.newBuilder()
            .header("Cache-Control", "public, max-age=$maxAge")
            .removeHeader("Pragma")
            .build()
    }

    private val offlineInterceptor: Interceptor = Interceptor { chain ->
        var request = chain.request()
        if (!NetworkUtil.isNetworkAvailable()) {
            val maxStale = 60 * 60 * 24 * 7
            request = request.newBuilder()
                .header("Cache-Control", "public, only-if-cached, max-stale=$maxStale")
                .removeHeader("Pragma")
                .build()
        }
        chain.proceed(request)
    }

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val credentials = "$API_KEY:$ACCESS_TOKEN"
        val request = original.newBuilder()
            .header(
                "Authorization",
                "Basic " + Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
            )
            .method(original.method, original.body)
            .build()
        chain.proceed(request)
    }

    private val cache =
        Cache(File(MyApplication.instance.cacheDir, "http-cache"), CACHE_SIZE.toLong())

    private val client: OkHttpClient = OkHttpClient.Builder()
        .cache(cache)
        .addInterceptor(authInterceptor)
        .addInterceptor(offlineInterceptor)
        .addNetworkInterceptor(onlineInterceptor)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .client(client)
        .build()

    val service = retrofit.create(AddressService::class.java)
}
