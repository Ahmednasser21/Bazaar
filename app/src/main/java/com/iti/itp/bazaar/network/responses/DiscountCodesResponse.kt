package com.iti.itp.bazaar.network.responses

import com.google.gson.annotations.SerializedName
import com.iti.itp.bazaar.dto.DiscountCode

data class DiscountCodesResponse(
    @SerializedName("discount_codes") val discountCodes: List<DiscountCode>
)