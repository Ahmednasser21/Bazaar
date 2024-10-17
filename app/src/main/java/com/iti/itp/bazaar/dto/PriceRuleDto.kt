package com.iti.itp.bazaar.dto

import com.google.gson.annotations.SerializedName


data class PriceRuleDto(
    @SerializedName("id") val id: Long,
    @SerializedName("value_type") val valueType: String,
    @SerializedName("value") val value: String,
    @SerializedName("customer_selection") val customerSelection: String,
    @SerializedName("target_type") val targetType: String,
    @SerializedName("target_selection") val targetSelection: String,
    @SerializedName("allocation_method") val allocationMethod: String,
    @SerializedName("allocation_limit") val allocationLimit: Any?,
    @SerializedName("once_per_customer") val oncePerCustomer: Boolean,
    @SerializedName("usage_limit") val usageLimit: Any?,
    @SerializedName("starts_at") val startsAt: String,
    @SerializedName("ends_at") val endsAt: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("entitled_product_ids") val entitledProductIds: List<Any>,
    @SerializedName("entitled_variant_ids") val entitledVariantIds: List<Any>,
    @SerializedName("entitled_collection_ids") val entitledCollectionIds: List<Any>,
    @SerializedName("entitled_country_ids") val entitledCountryIds: List<Any>,
    @SerializedName("prerequisite_product_ids") val prerequisiteProductIds: List<Any>,
    @SerializedName("prerequisite_variant_ids") val prerequisiteVariantIds: List<Any>,
    @SerializedName("prerequisite_collection_ids") val prerequisiteCollectionIds: List<Any>,
    @SerializedName("customer_segment_prerequisite_ids") val customerSegmentPrerequisiteIds: List<Any>,
    @SerializedName("prerequisite_customer_ids") val prerequisiteCustomerIds: List<Any>,
    @SerializedName("prerequisite_subtotal_range") val prerequisiteSubtotalRange: Any?,
    @SerializedName("prerequisite_quantity_range") val prerequisiteQuantityRange: Any?,
    @SerializedName("prerequisite_shipping_price_range") val prerequisiteShippingPriceRange: Any?,
    @SerializedName("prerequisite_to_entitlement_quantity_ratio") val prerequisiteToEntitlementQuantityRatio: PrerequisiteToEntitlementQuantityRatio,
    @SerializedName("prerequisite_to_entitlement_purchase") val prerequisiteToEntitlementPurchase: PrerequisiteToEntitlementPurchase,
    @SerializedName("title") val title: String,
    @SerializedName("admin_graphql_api_id") val adminGraphqlApiId: String
)

data class PrerequisiteToEntitlementQuantityRatio(
    @SerializedName("prerequisite_quantity") val prerequisiteQuantity: Any?,
    @SerializedName("entitled_quantity") val entitledQuantity: Any?
)

data class PrerequisiteToEntitlementPurchase(
    @SerializedName("prerequisite_amount") val prerequisiteAmount: Any?
)