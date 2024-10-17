package com.iti.itp.bazaar.dto


data class CustomerAddress(
    val id: Long? = null,
    val customer_id: Long? = null,
    val first_name: String = "",
    val last_name: String = "",
    val company: String? = null,
    val address1: String = "",
    val address2: String? = null,
    val city: String = "",
    val province: String? = null,
    val country: String = "",
    val zip: String = "",
    val phone: String = "",
    val name: String = "",
    val province_code: String? = null,
    val country_code: String? = null,
    val country_name: String = "",
    val default: Boolean = false
)

// For sending a new address request
data class AddressRequest(
    val address: CustomerAddress
)


data class CustomerAddressResponse(
    val customer_address: CustomerAddress
)

data class ListOfAddresses(
    val addresses: List<CustomerAddress>
)