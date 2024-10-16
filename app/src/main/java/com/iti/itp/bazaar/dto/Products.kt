package com.iti.itp.bazaar.dto

data class Products(
    val id: Long,
    val title: String,
    val bodyHtml: String,
    val vendor: String,
    val productType: String,
    val createdAt: String,
    val handle: String,
    val updatedAt: String,
    val publishedAt: String,
    val templateSuffix: String?,
    val publishedScope: String,
    val tags: String,
    val status: String,
    val adminGraphqlApiId: String,
    val variants: List<Variant>,
    val options: List<Option>,
    val images: List<Image>,
    val image: Image?
)

data class Variant(
    val id: Long,
    val productId: Long,
    val title: String,
    val price: String,
    val position: Int,
    val inventoryPolicy: String,
    val compareAtPrice: String?,
    val option1: String,
    val option2: String?,
    val option3: String?,
    val createdAt: String,
    val updatedAt: String,
    val taxable: Boolean,
    val barcode: String?,
    val fulfillmentService: String,
    val grams: Int,
    val inventoryManagement: String,
    val requiresShipping: Boolean,
    val sku: String,
    val weight: Int,
    val weightUnit: String,
    val inventoryItemId: Long,
    val inventoryQuantity: Int,
    val oldInventoryQuantity: Int,
    val adminGraphqlApiId: String,
    val imageId: Long?
)

data class Option(
    val id: Long,
    val productId: Long,
    val name: String,
    val position: Int,
    val values: List<String>
)

data class Image(
    val id: Long,
    val alt: String?,
    val position: Int,
    val productId: Long,
    val createdAt: String,
    val updatedAt: String,
    val adminGraphqlApiId: String,
    val width: Int,
    val height: Int,
    val src: String,
    val variantIds: List<Long>
)