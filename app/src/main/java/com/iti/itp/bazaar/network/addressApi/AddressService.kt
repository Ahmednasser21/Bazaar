package com.iti.itp.bazaar.network.addressApi

import com.iti.itp.bazaar.dto.CustomerAddress
import com.iti.itp.bazaar.dto.CustomerAddressResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface AddressService {
    @POST("admin/api/2024-10/customers/{customer_id}/addresses.json")
    suspend fun createCustomerAddress(
        @Path("customer_id") customerId: Long,
        @Body address: CustomerAddress
    ):Response<CustomerAddressResponse>
}