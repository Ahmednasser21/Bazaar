package com.iti.itp.bazaar.network.paypalApi

data class AccessTokenResponse(val access_token: String)

data class OrderRequest(
    val intent: String,
    val purchase_units: List<PurchaseUnit>,
    val payment_source: PaymentSource
)

data class PurchaseUnit(
    val reference_id: String,
    val amount: Amount
)
data class PaymentSource(
    val paypal: PayPalDetails
)

data class PayPalDetails(
    val experience_context: ExperienceContext
)

data class ExperienceContext(
    val payment_method_preference: String,
    val brand_name: String,
    val locale: String,
    val landing_page: String,
    val shipping_preference: String,
    val user_action: String,
    val return_url: String,
    val cancel_url: String
)

class EmptyBody


data class Amount(val currency_code: String, val value: String)

data class OrderResponse(val id: String)

data class CaptureResponse(val status: String)
