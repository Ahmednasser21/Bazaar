package com.iti.itp.bazaar.network.responses

import com.google.gson.annotations.SerializedName
import com.iti.itp.bazaar.dto.PriceRuleDto

data class PriceRulesResponse(
    @SerializedName("price_rules") val priceRules: List<PriceRuleDto>
)
