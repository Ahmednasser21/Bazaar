package com.iti.itp.bazaar.dto.cutomerResponce

import com.iti.itp.bazaar.dto.Address

data class CustomerResponse(
    val customer: Customers
)
data class CustomerByEmailResponce (
    val customers: List<Customers>
)
data class Customers(
    val id: Long,
    val email: String,
    val created_at: String,
    val updated_at: String,
    val first_name: String,
    val last_name: String,
    val orders_count: Int,
    val state: String,
    val total_spent: String,
    val last_order_id: Long?,
    val note: String?,
    val verified_email: Boolean,
    val multipass_identifier: String?,
    val tax_exempt: Boolean,
    val tags: String,
    val last_order_name: String?,
    val currency: String,
    val phone: String,
    val addresses: List<Address>,
    val tax_exemptions: List<String>,
    val email_marketing_consent: EmailMarketingConsent,
    val sms_marketing_consent: SmsMarketingConsent,
    val admin_graphql_api_id: String,
    val default_address: Address
)

data class Address(
    val id: Long,
    val customer_id: Long,
    val first_name: String,
    val last_name: String,
    val company: String?,
    val address1: String,
    val address2: String?,
    val city: String,
    val province: String,
    val country: String,
    val zip: String,
    val phone: String,
    val name: String,
    val province_code: String,
    val country_code: String,
    val country_name: String,
    val default: Boolean
)

data class EmailMarketingConsent(
    val state: String,
    val opt_in_level: String,
    val consent_updated_at: String?
)

data class SmsMarketingConsent(
    val state: String,
    val opt_in_level: String,
    val consent_updated_at: String?,
    val consent_collected_from: String
)