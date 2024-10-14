package com.iti.itp.bazaar.network

import com.iti.itp.bazaar.network.reponces.ProductsResponse

class RemoteDataSource(private val productService:ProductService) {

    suspend fun getProducts(fields:String): ProductsResponse{
       return productService.getProducts(fields)
    }
}