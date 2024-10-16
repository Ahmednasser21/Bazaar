package com.iti.itp.bazaar.repo

import com.iti.itp.bazaar.dto.CustomerAddress
import com.iti.itp.bazaar.network.reponces.ProductResponse
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class Repository private constructor(private val remoteDataSource: ShopifyRemoteDataSource) {

    companion object {
        private var INSTANCE: Repository? = null
        fun getInstance(
            remoteDataSource: ShopifyRemoteDataSource
        ): Repository {
            return INSTANCE ?: synchronized(this) {
                val instance = Repository(remoteDataSource)
                INSTANCE = instance
                instance
            }
        }
    }
    fun getVendors(fields:String):Flow<ProductResponse>{
        return flow{
            val vendorList = remoteDataSource.getVendors(fields)
            emit(vendorList)
            delay(100)
        }
    }

    fun getVendorProducts(vendorName:String):Flow<ProductResponse>{
        return flow {
            val vendorProducts = remoteDataSource.getVendorProducts(vendorName)
            emit(vendorProducts)
            delay(100)
        }
    }


    suspend fun createCustomerAddress(customerId: Long, address: CustomerAddress): Result<CustomerAddress> {
        return try {
            val response = remoteDataSource.addAddress(customerId, address)
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