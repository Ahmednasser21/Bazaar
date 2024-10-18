package com.iti.itp.bazaar.network.products

import com.google.gson.annotations.SerializedName

data class Products(
    val id: Long,
    val title: String,
    @SerializedName("body_html") val bodyHtml: String,
    val vendor: String,
    @SerializedName("product_type") val productType: String,
    @SerializedName("created_at") val createdAt: String,
    val handle: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("published_at") val publishedAt: String,
    @SerializedName("template_suffix") val templateSuffix: String?,
    @SerializedName("published_scope") val publishedScope: String,
    val tags: String,
    val status: String,
    @SerializedName("admin_graphql_api_id") val adminGraphqlApiId: String,
    val variants: List<Variant>,
    val options: List<Option>, // option
    val images: List<Image>,
    val image: Image?
)

data class Variant(
    val id: Long,
    @SerializedName("product_id") val productId: Long,
    val title: String,
    val price: String,
    val position: Int,
    @SerializedName("inventory_policy") val inventoryPolicy: String,
    @SerializedName("compare_at_price") val compareAtPrice: String?,
    val option1: String,
    val option2: String?,
    val option3: String?,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    val taxable: Boolean,
    val barcode: String?,
    @SerializedName("fulfillment_service") val fulfillmentService: String,
    val grams: Int,
    @SerializedName("inventory_management") val inventoryManagement: String,
    @SerializedName("requires_shipping") val requiresShipping: Boolean,
    val sku: String,
    val weight: Int,
    @SerializedName("weight_unit") val weightUnit: String,
    @SerializedName("inventory_item_id") val inventoryItemId: Long,
    @SerializedName("inventory_quantity") val inventoryQuantity: Int,
    @SerializedName("old_inventory_quantity") val oldInventoryQuantity: Int,
    @SerializedName("admin_graphql_api_id") val adminGraphqlApiId: String,
    @SerializedName("image_id") val imageId: Long?
)

data class Option(
    val id: Long,
    @SerializedName("product_id") val productId: Long,
    val name: String,
    val position: Int,
    val values: List<String>
)

data class Image(
    val id: Long,
    val alt: String?,
    val position: Int,
    @SerializedName("product_id") val productId: Long,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("admin_graphql_api_id") val adminGraphqlApiId: String,
    val width: Int,
    val height: Int,
    val src: String,
    @SerializedName("variant_ids") val variantIds: List<Long>
)
