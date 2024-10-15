package com.iti.itp.bazaar.network.responses

import com.iti.itp.bazaar.network.dto.Products

data class ProductResponse(
    val products: List<Products>
)
