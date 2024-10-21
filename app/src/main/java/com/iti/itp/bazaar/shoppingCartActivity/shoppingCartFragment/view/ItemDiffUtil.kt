package com.iti.itp.bazaar.shoppingCartActivity.shoppingCartFragment.view

import ReceivedLineItem
import androidx.recyclerview.widget.DiffUtil
import com.iti.itp.bazaar.dto.LineItem

class ItemDiffUtil : DiffUtil.ItemCallback<LineItem>() {
    override fun areItemsTheSame(oldItem: LineItem, newItem: LineItem
    ): Boolean {
        return oldItem.title == newItem.title
    }

    override fun areContentsTheSame(oldItem: LineItem, newItem: LineItem): Boolean {
        return oldItem == newItem
    }
}