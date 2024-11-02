package com.iti.itp.bazaar.mainActivity.favoriteProducts

import com.iti.itp.bazaar.dto.LineItem

interface OnFavProductDelete {
    fun onFavDelete(lineItem : LineItem)
}