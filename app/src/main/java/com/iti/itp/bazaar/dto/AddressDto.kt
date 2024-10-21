package com.iti.itp.bazaar.dto

import com.iti.itp.bazaar.dto.Address


data class CustomerAddress(
    val id: Long,
    val customer_id: Long,
    val first_name: String?,
    val last_name: String?,
    val company: String?,
    val address1: String?,
    val address2: String?,
    val city: String?,
    val province: String?,
    val country: String?,
    val zip: String?,
    val phone: String?,
    val name: String?,
    val province_code: String?,
    val country_code: String?,
    val country_name: String?,
    val default: Boolean?
)

data class AddAddressResponse(val customer_address:CustomerAddress)

data class AddedCustomerAddress(
    val address1: String = "",
    val address2: String?= "",
    val city: String= "",
    val company: String?= "",
    val first_name: String= "",
    val last_name: String= "",
    val phone: String= "",
    val province: String= "",
    val country: String= "",
    val zip: String= "",
    val name: String= "",
    val province_code: String= "",
    val country_code: String= "",
    val country_name: String= ""
)

data class AddedAddressRequest(
    val address: AddedCustomerAddress
)


data class AddedCustomerAddressResponse(
    val customer_address: AddedCustomerAddress
)



data class UpdateAddressRequest(
    val address: Address
)



data class AddressRequest(
    val address: AddressDetails
)

data class AddressDetails(
    val address1: String,
    val address2: String = "",               // Default to empty string
    val city: String,
    val company: String? = null,             // Nullable for optional values
    val first_name: String,
    val last_name: String,
    val phone: String,
    val province: String,
    val country: String,
    val zip: String,
    val name: String,
    val province_code: String,
    val country_code: String,
    val country_name: String)




data class CustomerAddressResponse(
    val customer_address: CustomerAddress
)

data class ListOfAddresses(
    val addresses: List<CustomerAddress>
)
