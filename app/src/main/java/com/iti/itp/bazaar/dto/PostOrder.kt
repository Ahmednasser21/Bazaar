package com.iti.itp.bazaar.dto

import com.iti.itp.bazaar.dto.Customer
import com.iti.itp.bazaar.dto.LineItem

data class DraftOrderRequest(
    val draft_order: DraftOrder
)

data class DraftOrder(
    var line_items: List<LineItem>,
    val applied_discount: AppliedDiscount?,
    val customer: Customer,
    val use_customer_default_address: Boolean
)


data class LineItem(
    val id: Long?= null,
    val variant_id: Long?= null,
    val product_id: Long,
    val title: String?,
    val variant_title: String?= null,
    var sku: String?= null,
    val vendor: String?= null,
    val quantity: Int?= null,
    val requires_shipping: Boolean?= null,
    val taxable: Boolean?= null,
    val gift_card: Boolean?= null,
    val fulfillment_service: String?= null,
    val grams: Int?= null,
    val tax_lines: List<Any>?= null,
    val applied_discount: Any?= null,
    val name: String?= null,
    val properties: List<Any>?= null,
    val custom: Boolean?= null,
    val price: String,
    val admin_graphql_api_id: String?= null
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