package com.iti.itp.bazaar.network.shopify

import com.iti.itp.bazaar.network.responses.VendorResponse

class ShopifyRemoteDataSource(private val productService: ProductService) {

    suspend fun getProducts(fields:String): VendorResponse{
       return productService.getVendors(fields)
    }
}