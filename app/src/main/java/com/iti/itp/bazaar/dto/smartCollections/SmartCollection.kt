package com.iti.itp.bazaar.dto.smartCollections

import com.google.gson.annotations.SerializedName
import com.iti.itp.bazaar.network.products.Image

data class SmartCollection(
    val id: Long,
    val handle: String,
    val title: String,
    @SerializedName("updated_at") val updatedAt: String,
    @SerializedName("body_html") val bodyHtml: String?,
    @SerializedName("published_at") val publishedAt: String,
    @SerializedName("sort_order") val sortOrder: String,
    @SerializedName("template_suffix") val templateSuffix: String?,
    val disjunctive: Boolean,
    val rules: List<Rule>,
    @SerializedName("published_scope") val publishedScope: String,
    @SerializedName("admin_graphql_api_id") val adminGraphqlApiId: String,
    val image: Image?
)

data class Rule(
    val column: String,
    val relation: String,
    val condition: String
)

data class Image(
    @SerializedName("created_at") val createdAt: String,
    val alt: String?,
    val width: Int,
    val height: Int,
    val src: String
)