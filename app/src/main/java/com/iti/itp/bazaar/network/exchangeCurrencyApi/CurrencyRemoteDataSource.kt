package com.iti.itp.bazaar.network.exchangeCurrencyApi

import com.iti.itp.bazaar.network.responses.ExchangeRateResponse

class CurrencyRemoteDataSource(val currencyService: CurrencyServiceApi) {

    suspend fun getExchangeRate(base:String, target:String): ExchangeRateResponse {
        return currencyService.getExchangeRate(base, target)
    }
}