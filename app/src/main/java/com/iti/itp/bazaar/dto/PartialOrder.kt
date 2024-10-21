package com.iti.itp.bazaar.dto
data class PartialOrder2(
    val order:PartialOrder
)
data class PartialOrder(
    var customer: OrderCustomer? = null,
    var line_items: List<OrderLineItem>? = null,
    var applied_discount: OrderAppliedDiscount? = null,
    var billing_address: OrderAddress? = null,
    var shipping_address: OrderAddress? = null,
    var fulfillment_status: String? = null,
    var payment_gateway_names: List<String>? = null
)

data class OrderCustomer(
    val id: Long
)

data class OrderLineItem(
    val variant_id: Long,
    val quantity: Int,
    val name: String,
    val title: String,
    val price: String
)

data class OrderAppliedDiscount(
    val description: String,
    val value: String,
    val value_type: String,
    val amount: String,
    val title: String
)

data class OrderAddress(
    val first_name: String,
    val last_name: String,
    val address1: String,
    val address2: String?,
    val city: String,
    val province: String,
    val country: String,
    val zip: String,
    val phone: String
)