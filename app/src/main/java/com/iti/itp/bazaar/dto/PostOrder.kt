package com.iti.itp.bazaar.dto

data class DraftOrderRequest(
    val draft_order: DraftOrder
)

data class DraftOrder(
    val line_items: List<LineItem>,
    val applied_discount: AppliedDiscount?,
    val customer: Customer,
    val use_customer_default_address: Boolean
)

data class LineItem(
    val title: String,
    val price: String,  // Using String to maintain decimal precision
    val quantity: Int
)

data class AppliedDiscount(
    val description: String? = null,
    val value_type: String? = null,  // Could be made into enum if all possible values are known
    val value: String? = null,      // Using String to maintain decimal precision
    val amount: String? = null,     // Using String to maintain decimal precision
    val title: String? = null
)

data class Customer(
    val id: Long
)