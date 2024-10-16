package com.iti.itp.bazaar.network.addressApi

import com.iti.itp.bazaar.dto.CustomerAddress
import com.iti.itp.bazaar.dto.CustomerAddressResponse
import retrofit2.Call
import retrofit2.Response

class AddressRemoteDataSource(private val addressService: AddressService) {
    suspend fun addAddress(customerId: Long, customerAddress: CustomerAddress): Response<CustomerAddressResponse> {
        return addressService.createCustomerAddress(customerId,customerAddress)
    }
}
