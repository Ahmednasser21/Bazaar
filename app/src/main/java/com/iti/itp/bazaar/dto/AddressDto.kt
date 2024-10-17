package com.iti.itp.bazaar.dto


data class CustomerAddress(
    val customer_id: Long?=null,
    val zip: String ="",
    val country: String="",
    val province: String? = null,
    val city: String="",
    val address1: String="",
    val address2: String?="",
    val first_name: String?="",
    val last_name: String?="",
    val company: String?="",
    val phone: String="",
    val id: Long?=null,
    val name: String="",
    val province_code: String? = null,
    val country_code: String="",
    val country_name: String="",
    val default: Boolean = false
)

data class Address(
    val id: Long,
    val zip: String =""
)

data class UpdateAddressRequest(
    val address: Address
)



data class AddressRequest(
    val customer_address: AddressDetails
)

data class AddressDetails(
    val id: Long? =null,
    val customer_id: Long?=null,
    val first_name: String,
    val last_name: String? = null, // Nullable to allow null values
    val company: String="",
    val address1: String ="",
    val address2: String = "",       // Default to empty string
    val city: String,
    val province: String = "",       // Default to empty string
    val country: String,        // Default to empty string
    val zip: String = "",            // Default to empty string
    val phone: String,
    val name: String="",
    val province_code: String? = null, // Nullable
    val country_code: String ?= null,  // Nullable
    val country_name: String = "",      // Default to empty string
    val default: Boolean = false
)




data class CustomerAddressResponse(
    val customer_address: CustomerAddress
)

data class ListOfAddresses(
    val addresses: List<CustomerAddress>
)
