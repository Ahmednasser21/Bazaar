package com.iti.itp.bazaar.repo

import com.iti.itp.bazaar.network.exchangeCurrencyApi.CurrencyRemoteDataSource
import com.iti.itp.bazaar.network.responses.ExchangeRateResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class CurrencyRepository(private val currencyRemoteDataSource: CurrencyRemoteDataSource) {
    fun getExchangeRate(base: String, target: String): Flow<ExchangeRateResponse> = flow {

        val response = currencyRemoteDataSource.getExchangeRate(base, target)
        emit(response)
    }
}