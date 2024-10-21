package com.iti.itp.bazaar.dto


data class CustomerRequest(
    var customer: PostedCustomer
)

data class PostedCustomer(
    val first_name: String,
    val last_name: String,
    var email: String,
    var phone: String,
    val verified_email: Boolean,
    val addresses: List<Address>,
    var password: String,
    var password_confirmation: String,
    val send_email_welcome: Boolean
)

data class Address(
    val address1: String,
    val city: String,
    val province: String,
    val phone: String,
    val zip: String,
    val last_name: String,
    val first_name: String,
    val country: String
)



data class UpdateCustomerRequest(
    val customer: CustomerUpdate
)

data class CustomerUpdate(
    var id: Long,
    var first_name: String? = null,
    var last_name: String? = null,
    val email: String? = null,
)

