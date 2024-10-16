package com.iti.itp.bazaar.network.responses
import com.google.gson.annotations.SerializedName
import com.iti.itp.bazaar.dto.smartCollections.SmartCollection

data class SmartCollectionsResponse(
    @SerializedName("smart_collections") val smartCollections: List<SmartCollection>
)
