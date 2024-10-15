package com.iti.itp.bazaar.repo

import com.iti.itp.bazaar.network.RemoteDataSource
import com.iti.itp.bazaar.network.responses.VendorResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class Repository (private val remoteDataSource: RemoteDataSource) {

    companion object {
        private var INSTANCE: Repository? = null
        fun getInstance(
            remoteDataSource: RemoteDataSource,
        ): Repository {
            return INSTANCE ?: synchronized(this) {
                val instance = Repository(remoteDataSource)
                INSTANCE = instance
                instance
            }
        }
    }
    suspend fun getProducts(fields:String):Flow<VendorResponse>{
        return flow{
            val vendorList = remoteDataSource.getProducts(fields)
            emit(vendorList)
            delay(100)
        }
    }
}