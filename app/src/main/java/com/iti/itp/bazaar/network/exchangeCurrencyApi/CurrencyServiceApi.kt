package com.iti.itp.bazaar.network.exchangeCurrencyApi

import com.iti.itp.bazaar.network.responses.ExchangeRateResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface CurrencyServiceApi {
    @GET("5504505647a7680b0d8c74be/pair/{base}/{target}")
    suspend fun getExchangeRate(@Path("base") base: String, @Path("target") target: String): ExchangeRateResponse
}
