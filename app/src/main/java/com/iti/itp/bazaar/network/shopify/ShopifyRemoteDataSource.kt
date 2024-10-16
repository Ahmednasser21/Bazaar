package com.iti.itp.bazaar.network.shopify

import com.iti.itp.bazaar.dto.CustomerAddress
import com.iti.itp.bazaar.dto.CustomerAddressResponse
import com.iti.itp.bazaar.network.responses.ProductResponse
import retrofit2.Response

class ShopifyRemoteDataSource(private val productService: ProductService) {

    suspend fun getVendors(fields:String): ProductResponse{
       return productService.getVendors(fields)
    }

    suspend fun getVendorProducts(vendorName:String):ProductResponse{
        return productService.getVendorProducts(vendorName)
    }

    suspend fun addAddress(customerId: Long, customerAddress: CustomerAddress): Response<CustomerAddressResponse> {
        return productService.createCustomerAddress(customerId,customerAddress)
    }
}