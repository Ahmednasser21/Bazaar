package com.iti.itp.bazaar.favoriteProducts

import com.iti.itp.bazaar.network.products.Products

interface OnFavProductCardClick {

    fun onCardClick(productId : Long)
}