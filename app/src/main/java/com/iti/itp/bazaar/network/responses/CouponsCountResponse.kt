package com.iti.itp.bazaar.network.responses

import com.google.gson.annotations.SerializedName

data class CouponsCountResponse(
    @SerializedName("count") val count: Int
)


