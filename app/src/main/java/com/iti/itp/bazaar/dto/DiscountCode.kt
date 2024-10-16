package com.iti.itp.bazaar.dto

import com.google.gson.annotations.SerializedName


data class DiscountCode(
    @SerializedName("id") val id: Long,
    @SerializedName("price_rule_id") val priceRuleId: Long,
    @SerializedName("code") val code: String,
    @SerializedName("usage_count") val usageCount: Int,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String
)