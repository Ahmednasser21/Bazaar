package com.iti.itp.bazaar.mainActivity.ui.products

import com.iti.itp.bazaar.network.products.Products

interface OnFavouriteClickListener {
    fun onFavProductClick(product:Products)
}