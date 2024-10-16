package com.iti.itp.bazaar.network.reponces
import com.google.gson.annotations.SerializedName
import com.iti.itp.bazaar.network.smartCollections.SmartCollection

data class SmartCollectionsResponse(
    @SerializedName("smart_collections") val smartCollections: List<SmartCollection>
)
