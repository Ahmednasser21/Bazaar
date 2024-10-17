package com.iti.itp.bazaar.network.shopify

import com.iti.itp.bazaar.network.responses.ProductResponse

class ShopifyRemoteDataSource(private val productService: ProductService) {

    suspend fun getVendors(fields:String): ProductResponse{
       return productService.getVendors(fields)
    }

    suspend fun getVendorProducts(vendorName:String):ProductResponse{
        return productService.getVendorProducts(vendorName)
    }
    suspend fun getProductDetails(id: Long):ProductResponse{
        return productService.getProductDetails (id)
    }

}