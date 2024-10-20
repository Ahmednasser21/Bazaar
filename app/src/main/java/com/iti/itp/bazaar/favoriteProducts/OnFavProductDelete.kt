package com.iti.itp.bazaar.favoriteProducts

import com.iti.itp.bazaar.dto.LineItem

interface OnFavProductDelete {
    fun onFavDelete(lineItem : LineItem)
}