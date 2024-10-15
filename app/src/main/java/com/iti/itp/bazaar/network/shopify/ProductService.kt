package com.iti.itp.bazaar.network.shopify

import com.iti.itp.bazaar.network.responses.VendorResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ProductService {
    @GET("admin/api/2022-01/products.json")
    suspend fun getVendors(@Query("fields") fields: String): VendorResponse

    @GET("admin/api/2022-01/products.json")
    suspend fun getProduct(@Query("fields") fields: String): VendorResponse
}