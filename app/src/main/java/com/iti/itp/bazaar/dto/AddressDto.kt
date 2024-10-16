package com.iti.itp.bazaar.dto

data class CustomerAddress(
    val id: Long,
    val customer_id: Long,
    val first_name: String = "",
    val last_name: String = "",
    val company: String ="",
    val address1: String="",
    val address2: String="",
    val city: String="",
    val province: String="",
    val country: String="",
    val zip: String="",
    val phone: String="",
    val name: String="",
    val province_code: String="",
    val country_code: String="",
    val country_name: String="",
    val default: Boolean=false
)

data class CustomerAddressResponse(
    val customer_address: CustomerAddress
)