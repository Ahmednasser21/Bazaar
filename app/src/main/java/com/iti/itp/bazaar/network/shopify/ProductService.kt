package com.iti.itp.bazaar.network.shopify

import com.iti.itp.bazaar.network.responses.ProductResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ProductService {
    @GET("admin/api/2022-01/products.json")
    suspend fun getVendors(@Query("fields") fields: String): ProductResponse

    @GET("admin/api/2022-01/products.json")
    suspend fun getVendorProducts(@Query("vendor") name: String): ProductResponse
}
