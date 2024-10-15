package com.iti.itp.bazaar.network

import com.iti.itp.bazaar.network.responses.VendorResponse

class RemoteDataSource(private val productService:ProductService) {

    suspend fun getProducts(fields:String): VendorResponse{
       return productService.getVendors(fields)
    }
}