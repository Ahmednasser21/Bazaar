package com.iti.itp.bazaar.dto.order

import com.google.gson.annotations.SerializedName

data class Order(
    val id: Long,

    @SerializedName("admin_graphql_api_id")
    val adminGraphqlApiId: String,

    @SerializedName("app_id")
    val appId: Long,

    @SerializedName("browser_ip")
    val browserIp: String?,

    val buyerAcceptsMarketing: Boolean,

    @SerializedName("cancel_reason")
    val cancelReason: String?,

    @SerializedName("cancelled_at")
    val cancelledAt: String?,

    @SerializedName("cart_token")
    val cartToken: String?,

    @SerializedName("checkout_id")
    val checkoutId: String?,

    @SerializedName("checkout_token")
    val checkoutToken: String?,

    @SerializedName("client_details")
    val clientDetails: String?,

    @SerializedName("closed_at")
    val closedAt: String?,

    val company: String?,

    val confirmationNumber: String,
    val confirmed: Boolean,
    val contactEmail: String,
    @SerializedName("created_at")
    val createdAt: String,
    val currency: String,

    @SerializedName("current_subtotal_price")
    val currentSubtotalPrice: String,

    @SerializedName("current_subtotal_price_set")
    val currentSubtotalPriceSet: PriceSet,

    @SerializedName("current_total_discounts")
    val currentTotalDiscounts: String,

    @SerializedName("current_total_discounts_set")
    val currentTotalDiscountsSet: PriceSet,

    @SerializedName("current_total_price")
    val currentTotalPrice: String,

    @SerializedName("current_total_price_set")
    val currentTotalPriceSet: PriceSet,

    @SerializedName("current_total_tax")
    val currentTotalTax: String,

    @SerializedName("current_total_tax_set")
    val currentTotalTaxSet: PriceSet,

    val customerLocale: String?,
    val deviceId: String?,

    @SerializedName("discount_codes")
    val discountCodes: List<String>,

    val email: String,
    val estimatedTaxes: Boolean,
    val financialStatus: String,
    val fulfillmentStatus: String?,
    val landingSite: String?,
    val locationId: String?,
    val name: String,
    val note: String?,
    val number: Int,
    val orderNumber: Int,
    val orderStatusUrl: String,
    val paymentGatewayNames: List<String>,
    val phone: String?,
    val processedAt: String,
    val subtotalPrice: String,

    @SerializedName("subtotal_price_set")
    val subtotalPriceSet: PriceSet,

    val tags: String,
    val taxExempt: Boolean,
    val taxesIncluded: Boolean,
    val test: Boolean,
    val token: String,
    val totalDiscounts: String,

    @SerializedName("total_discounts_set")
    val totalDiscountsSet: PriceSet,
    @SerializedName("total_price")
    val totalPrice: String,

    @SerializedName("total_price_set")
    val totalPriceSet: PriceSet,

    val billingAddress: Address,
    val customer: Customer,
    val lineItems: List<LineItem>,
    val shippingAddress: Address,
    val shippingLines: List<Any>
)

data class PriceSet(
    val shopMoney: Money,
    val presentmentMoney: Money
)

data class Money(
    val amount: String,
    val currencyCode: String
)

data class Address(
    val firstName: String,
    val lastName: String,
    val address1: String,
    val address2: String?,
    val phone: String,
    val city: String,
    val zip: String,
    val country: String,
    val latitude: Double?,
    val longitude: Double?,
    val countryCode: String
)

data class Customer(
    val id: Long,
    val email: String,
    val firstName: String,
    val lastName: String,
    val createdAt: String,
    val updatedAt: String,
    val verifiedEmail: Boolean,
    val tags: String,
    val currency: String
)

data class LineItem(
    val id: Long,
    val name: String,
    val price: String,
    val quantity: Int
)