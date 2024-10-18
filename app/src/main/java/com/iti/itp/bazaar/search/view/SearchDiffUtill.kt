package com.iti.itp.bazaar.search.view

import androidx.recyclerview.widget.DiffUtil
import com.iti.itp.bazaar.network.products.Products
import com.iti.itp.bazaar.productInfo.view.AvailableSizes

class SearchDiffUtill : DiffUtil.ItemCallback<Products>() {


    override fun areItemsTheSame(oldItem: Products, newItem: Products): Boolean {
     return oldItem.title == newItem.title
    }

    override fun areContentsTheSame(oldItem: Products, newItem: Products): Boolean {
        return oldItem == newItem
    }
}