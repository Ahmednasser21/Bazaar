package com.iti.itp.bazaar.network.responses

import com.iti.itp.bazaar.network.dto.Product

data class VendorResponse(
    val products: List<Product>)
