package com.iti.itp.bazaar.network.addressApi

import com.iti.itp.bazaar.dto.CustomerAddress


class AddressRepo(private val addressRemoteDataSource: AddressRemoteDataSource) {
    suspend fun createCustomerAddress(customerId: Long, address: CustomerAddress): Result<CustomerAddress> {
        return try {
            val response = AddressRetrofit.service.createCustomerAddress(customerId, address)
            if (response.isSuccessful) {
                Result.success(response.body()?.customer_address!!)
            } else {
                Result.failure(Exception("API call failed with code ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}