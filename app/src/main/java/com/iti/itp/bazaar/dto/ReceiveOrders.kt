data class ReceivedOrdersResponse(
    val draft_orders: List<ReceivedDraftOrder>
)

data class ReceivedDraftOrder(
    val id: Long,
    val note: String? = null,
    val email: String? = null,
    val taxes_included: Boolean? = null,
    val currency: String? = null,
    val invoice_sent_at: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val tax_exempt: Boolean? = null,
    val completed_at: String? = null,
    val name: String? ="",
    val status: String? = null,
    var line_items: List<ReceivedLineItem>? = null,
    val shipping_address: ReceivedAddress? = null,
    val billing_address: ReceivedAddress? = null,
    val invoice_url: String? = null,
    val applied_discount: ReceivedDiscount? = null,
    val order_id: Long?=null,
    val shipping_line: ReceivedShippingLine?=null,
    val tax_lines: List<Any>?=null,
    val tags: String?=null,
    val note_attributes: List<Any>?=null,
    val total_price: String?=null,
    val subtotal_price: String?=null,
    val total_tax: String?=null,
    val payment_terms: Any?=null,
    val presentment_currency: String?=null,
    val total_line_items_price_set: ReceivedPriceSet?=null,
    val total_price_set: ReceivedPriceSet?=null,
    val subtotal_price_set: ReceivedPriceSet?=null,
    val total_tax_set: ReceivedPriceSet?=null,
    val total_discounts_set: ReceivedPriceSet?=null,
    val total_shipping_price_set: ReceivedPriceSet?=null,
    val total_additional_fees_set: Any?=null,
    val total_duties_set: Any?=null,
    val admin_graphql_api_id: String?=null,
    val customer: ReceivedCustomer?=null
)

data class ReceivedLineItem(
    val id: Long?= null,
    val variant_id: Long?= null,
    val product_id: Long,
    val title: String?=null,
    val variant_title: String? = null,
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

data class ReceivedAddress(
    val first_name: String,
    val address1: String,
    val phone: String,
    val city: String,
    val zip: String,
    val province: String,
    val country: String,
    val last_name: String,
    val address2: String,
    val company: String?,
    val latitude: Double,
    val longitude: Double,
    val name: String,
    val country_code: String,
    val province_code: String
)

data class ReceivedDiscount(
    val description: String?,
    val value: String?,
    val title: String?,
    val amount: String?,
    val value_type: String?
)

data class ReceivedShippingLine(
    val title: String,
    val custom: Boolean,
    val handle: String?,
    val price: String
)

data class ReceivedPriceSet(
    val shop_money: ReceivedMoney,
    val presentment_money: ReceivedMoney
)

data class ReceivedMoney(
    val amount: String,
    val currency_code: String
)

data class ReceivedCustomer(
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
    val phone: String?,
    val tax_exemptions: List<Any>,
    val email_marketing_consent: ReceivedMarketingConsent,
    val sms_marketing_consent: ReceivedMarketingConsent,
    val admin_graphql_api_id: String,
    val default_address: ReceivedAddress?
)

data class ReceivedMarketingConsent(
    val state: String,
    val opt_in_level: String?,
    val consent_updated_at: String,
    val consent_collected_from: String?
)