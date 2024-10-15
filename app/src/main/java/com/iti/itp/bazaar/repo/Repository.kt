package com.iti.itp.bazaar.repo

import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.reponces.ProductsResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class Repository (private val shopifyRemoteDataSource: ShopifyRemoteDataSource) {

    companion object {
        private var INSTANCE: Repository? = null
        fun getInstance(
            shopifyRemoteDataSource: ShopifyRemoteDataSource,
        ): Repository {
            return INSTANCE ?: synchronized(this) {
                val instance = Repository(shopifyRemoteDataSource)
                INSTANCE = instance
                instance
            }
        }
    }
    suspend fun getProducts(fields:String):Flow<ProductsResponse>{
        return flow{
            val vendorList = shopifyRemoteDataSource.getProducts(fields)
            emit(vendorList)
            delay(100)
        }
    }
}