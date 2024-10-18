package com.iti.itp.bazaar.repo

import com.iti.itp.bazaar.dto.AddedAddressRequest
import com.iti.itp.bazaar.dto.AddedCustomerAddressResponse
import com.iti.itp.bazaar.dto.AddressRequest
import com.iti.itp.bazaar.dto.CustomerAddress
import com.iti.itp.bazaar.dto.CustomerAddressResponse
import com.iti.itp.bazaar.dto.CustomerRequest
import com.iti.itp.bazaar.dto.ListOfAddresses
import com.iti.itp.bazaar.dto.cutomerResponce.CustomerResponse
import com.iti.itp.bazaar.network.responses.CouponsCountResponse
import com.iti.itp.bazaar.network.responses.DiscountCodesResponse
import com.iti.itp.bazaar.network.responses.PriceRulesCountResponse
import com.iti.itp.bazaar.network.responses.PriceRulesResponse
import com.iti.itp.bazaar.network.responses.ProductResponse
import com.iti.itp.bazaar.network.responses.SmartCollectionsResponse
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
    fun getVendors():Flow<SmartCollectionsResponse>{
        return flow{
            val vendorList = remoteDataSource.getVendors()
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


    suspend fun createCustomerAddress(customerId: Long, address: AddedAddressRequest): Flow<AddedCustomerAddressResponse> {
        return flow {
            emit(remoteDataSource.addAddress(customerId, address))
        }
    }


    fun getPriceRules():Flow<PriceRulesResponse>{
        return flow {
            val priceRulesResponse = remoteDataSource.getPriceRules()
            emit(priceRulesResponse)
            delay(100)
        }
    }

    fun getPriceRulesCount():Flow<PriceRulesCountResponse>{
        return flow {
            val priceRulesCount = remoteDataSource.getPriceRulesCount()
            emit(priceRulesCount)
            delay(100)
        }
    }

    fun getCouponsCount():Flow<CouponsCountResponse>{
        return flow {
            val couponsCount = remoteDataSource.getCouponsCount()
            emit(couponsCount)
            delay(100)
        }
    }

    fun getCoupons(priceRuleId:Long):Flow<DiscountCodesResponse>{
        return flow {
            val coupons = remoteDataSource.getCoupons(priceRuleId)
            emit(coupons)
            delay(100)
        }
    }
    fun getCollectionProducts(id:Long):Flow<ProductResponse>{
        return flow{
            val collectionProductList = remoteDataSource.getCollectionProducts(id)
            emit(collectionProductList)
            delay(100)
        }
    }

    fun getAddressForCustomer(customerId:Long):Flow<ListOfAddresses>{
        return flow {
            emit(remoteDataSource.getAddressForCustomer(customerId))
        }
    }

    fun getProductDetails (id: Long): Flow<ProductResponse> {
        return flow {
            val ProductDetails = remoteDataSource.getProductDetails (id)
            emit(ProductDetails)
            delay(100)
        }
    }

    fun postCustomer (customer : CustomerRequest ): Flow<CustomerResponse> {
        return flow {
            val customerResponce = remoteDataSource.postCustomer (customer)
            emit(customerResponce)
            delay(100)
        }
    }

    fun updateCustomerAddress(customerId:Long, addressId:Long, customerAddress: CustomerAddressResponse):Flow<CustomerAddressResponse>{
        return flow {
            emit(remoteDataSource.updateCustomerAddress(customerId,addressId,customerAddress))
        }
    }
}