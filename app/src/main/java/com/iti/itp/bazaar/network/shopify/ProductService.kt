package com.iti.itp.bazaar.network.shopify

import com.iti.itp.bazaar.network.reponces.ProductsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ProductService {
    @GET("admin/api/2022-01/products.json")
    suspend fun getProducts(@Query("fields") fields: String): ProductsResponse
}