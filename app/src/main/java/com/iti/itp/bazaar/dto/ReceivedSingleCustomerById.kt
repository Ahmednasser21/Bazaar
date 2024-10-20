package com.iti.itp.bazaar.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import java.time.ZonedDateTime

data class SingleCustomerResponse(
    val customer: SingleCustomer
)

data class SingleCustomer(
    val id: Long,
    val email: String,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("updated_at")
    val updatedAt: String,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String,
    @SerializedName("orders_count")
    val ordersCount: Int,
    val state: String,
    @SerializedName("total_spent")
    val totalSpent: Double,
    @SerializedName("last_order_id")
    val lastOrderId: Long,
    val note: String?,
    @SerializedName("verified_email")
    val verifiedEmail: Boolean,
    @SerializedName("multipass_identifier")
    val multipassIdentifier: String?,
    @SerializedName("tax_exempt")
    val taxExempt: Boolean,
    val tags: String,
    @SerializedName("last_order_name")
    val lastOrderName: String,
    val currency: String,
    val phone: String,
    val addresses: List<SingleCustomerAddress>,
    @SerializedName("tax_exemptions")
    val taxExemptions: List<String>,
    @SerializedName("email_marketing_consent")
    val emailMarketingConsent: SingleCustomerMarketingConsent,
    @SerializedName("sms_marketing_consent")
    val smsMarketingConsent: SingleCustomerSmsMarketingConsent,
    @SerializedName("admin_graphql_api_id")
    val adminGraphqlApiId: String,
    @SerializedName("default_address")
    val defaultAddress: SingleCustomerAddress
)

data class SingleCustomerAddress(
    val id: Long,
    @SerializedName("customer_id")
    val customerId: Long,
    @SerializedName("first_name")
    val firstName: String?,
    @SerializedName("last_name")
    val lastName: String?,
    val company: String?,
    val address1: String,
    val address2: String,
    val city: String,
    val province: String,
    val country: String,
    val zip: String,
    val phone: String,
    val name: String,
    @SerializedName("province_code")
    val provinceCode: String,
    @SerializedName("country_code")
    val countryCode: String,
    @SerializedName("country_name")
    val countryName: String,
    val default: Boolean
)

data class SingleCustomerMarketingConsent(
    val state: String,
    @SerializedName("opt_in_level")
    val optInLevel: String?,
    @SerializedName("consent_updated_at")
    val consentUpdatedAt: ZonedDateTime
)

data class SingleCustomerSmsMarketingConsent(
    val state: String,
    @SerializedName("opt_in_level")
    val optInLevel: String,
    @SerializedName("consent_updated_at")
    val consentUpdatedAt: ZonedDateTime,
    @SerializedName("consent_collected_from")
    val consentCollectedFrom: String
)