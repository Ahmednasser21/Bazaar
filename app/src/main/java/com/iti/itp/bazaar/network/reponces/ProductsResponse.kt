package com.iti.itp.bazaar.network.reponces

import com.iti.itp.bazaar.network.dto.Product

data class ProductsResponse(
    val products: List<Product>)
