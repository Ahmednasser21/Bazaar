package com.iti.itp.bazaar.network.shopify

import com.iti.itp.bazaar.network.reponces.ProductsResponse

class ShopifyRemoteDataSource(private val productService: ProductService) {

    suspend fun getProducts(fields:String): ProductsResponse{
       return productService.getProducts(fields)
    }
}