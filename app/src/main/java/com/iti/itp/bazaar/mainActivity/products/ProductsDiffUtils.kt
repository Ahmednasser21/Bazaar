package com.iti.itp.bazaar.mainActivity.products

import androidx.recyclerview.widget.DiffUtil
import com.iti.itp.bazaar.network.products.Products

class ProductsDiffUtils :DiffUtil.ItemCallback<Products>() {
    override fun areItemsTheSame(oldItem: Products, newItem: Products): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Products, newItem: Products): Boolean {
        return oldItem == newItem
    }
}