package com.iti.itp.bazaar.network.exchangeCurrencyApi

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ExchangeRetrofitObj {
    private const val BASE_URL = "https://v6.exchangerate-api.com/v6/"

    private val instance = Retrofit.Builder().baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service = instance.create(CurrencyServiceApi::class.java)
}