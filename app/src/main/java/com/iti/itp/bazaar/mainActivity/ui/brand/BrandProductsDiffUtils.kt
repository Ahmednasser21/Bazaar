package com.iti.itp.bazaar.mainActivity.ui.brand

import androidx.recyclerview.widget.DiffUtil

class BrandProductsDiffUtils :DiffUtil.ItemCallback<BrandProductDTO>() {
    override fun areItemsTheSame(oldItem: BrandProductDTO, newItem: BrandProductDTO): Boolean {
        return oldItem.productID == newItem.productID
    }

    override fun areContentsTheSame(oldItem: BrandProductDTO, newItem: BrandProductDTO): Boolean {
        return oldItem == newItem
    }
}