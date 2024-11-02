package com.iti.itp.bazaar.mainActivity.products

import com.iti.itp.bazaar.network.products.Products

interface OnFavouriteClickListener {
    fun onFavProductClick(product:Products)
}