package com.iti.itp.bazaar.mainActivity.ui.brand

import androidx.recyclerview.widget.DiffUtil
import com.iti.itp.bazaar.network.products.Products

class BrandProductsDiffUtils :DiffUtil.ItemCallback<Products>() {
    override fun areItemsTheSame(oldItem: Products, newItem: Products): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Products, newItem: Products): Boolean {
        return oldItem == newItem
    }
}