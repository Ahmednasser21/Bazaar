package com.iti.itp.bazaar.dto

import com.iti.itp.bazaar.dto.LineItem

data class UpdateDraftOrderRequest(
    val draft_order: DraftOrder
)

data class UpdateDraftOrder(
    val id: Long,
    val line_items: List<LineItem>
)

data class UpdateLineItem(
    val variant_id: Long?= null,
    val product_id: Long?= null,
    val title: String,
    val variant_title: String?= null,
    val sku: String?= null,
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