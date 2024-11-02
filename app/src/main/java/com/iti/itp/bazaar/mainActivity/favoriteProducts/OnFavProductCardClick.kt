package com.iti.itp.bazaar.mainActivity.favoriteProducts

import com.iti.itp.bazaar.network.products.Products

interface OnFavProductCardClick {

    fun onCardClick(productId : Long)
}