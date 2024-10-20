package com.iti.itp.bazaar.favoriteProducts.view

import androidx.recyclerview.widget.DiffUtil
import com.iti.itp.bazaar.dto.LineItem

class FavoriteProductsDiffUtill : DiffUtil.ItemCallback<LineItem>() {
    override fun areItemsTheSame(oldItem: LineItem, newItem: LineItem): Boolean {
     return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: LineItem, newItem: LineItem): Boolean {
       return oldItem == newItem
    }
}