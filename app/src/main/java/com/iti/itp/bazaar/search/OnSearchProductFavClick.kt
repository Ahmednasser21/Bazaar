package com.iti.itp.bazaar.search

import com.iti.itp.bazaar.network.products.Products

interface OnSearchProductFavClick {

    fun onFavClick(prduct : Products)
}