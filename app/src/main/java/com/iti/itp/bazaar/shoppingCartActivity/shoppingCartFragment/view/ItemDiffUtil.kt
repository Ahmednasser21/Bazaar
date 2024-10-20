package com.iti.itp.bazaar.shoppingCartActivity.shoppingCartFragment.view

import ReceivedLineItem
import androidx.recyclerview.widget.DiffUtil

class ItemDiffUtil : DiffUtil.ItemCallback<ReceivedLineItem>() {
    override fun areItemsTheSame(oldItem: ReceivedLineItem, newItem: ReceivedLineItem
    ): Boolean {
        return oldItem.title == newItem.title
    }

    override fun areContentsTheSame(oldItem: ReceivedLineItem, newItem: ReceivedLineItem): Boolean {
        return oldItem == newItem
    }
}