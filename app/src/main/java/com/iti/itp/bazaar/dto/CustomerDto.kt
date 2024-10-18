package com.iti.itp.bazaar.dto



data class CustomerRequest(
    val postedCustomer: PostedCustomer
)

data class PostedCustomer(
    val first_name: String,
    val last_name: String,
    val email: String,
    val phone: String,
    val verified_email: Boolean,
    val addresses: List<Address>,
    val password: String,
    val password_confirmation: String,
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

