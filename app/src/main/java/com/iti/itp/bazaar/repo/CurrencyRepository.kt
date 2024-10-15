package com.iti.itp.bazaar.repo

import com.iti.itp.bazaar.network.exchangeCurrencyApi.CurrencyRemoteDataSource
import com.iti.itp.bazaar.network.reponces.ExchangeRateResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

class CurrencyRepository(private val currencyRemoteDataSource: CurrencyRemoteDataSource) {
    fun getExchangeRate(base: String, target: String): Flow<ExchangeRateResponse> = flow {

        val response = currencyRemoteDataSource.getExchangeRate(base, target)
        emit(response)
    }
}