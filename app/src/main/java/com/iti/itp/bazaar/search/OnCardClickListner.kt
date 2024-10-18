package com.iti.itp.bazaar.search

import com.iti.itp.bazaar.network.products.Products

interface OnCardClickListner {
    fun onCardClick(prduct : Products)
}