package com.iti.itp.bazaar.network.reponces

import com.iti.itp.bazaar.network.products.Products

data class ProductResponse(
    val products: List<Products>
)
