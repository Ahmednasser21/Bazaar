package com.iti.itp.bazaar.network.shopify

import com.iti.itp.bazaar.dto.CustomerAddress
import com.iti.itp.bazaar.dto.CustomerAddressResponse
import com.iti.itp.bazaar.network.responses.ProductResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductService {
    @GET("admin/api/2022-01/products.json")
    suspend fun getVendors(@Query("fields") fields: String): ProductResponse

    @GET("admin/api/2022-01/products.json")
    suspend fun getVendorProducts(@Query("vendor") name: String): ProductResponse
    ///
    @GET("admin/api/2022-01/products.json")
    suspend fun getProductDetails(@Query("ids") id: Long): ProductResponse

    @POST("admin/api/2024-10/customers/{customer_id}/addresses.json")
    suspend fun createCustomerAddress(
        @Path("customer_id") customerId: Long,
        @Body address: CustomerAddress
    ): Response<CustomerAddressResponse>
}
