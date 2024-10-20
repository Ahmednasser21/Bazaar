package com.iti.itp.bazaar.mainActivity.ui.order

import androidx.recyclerview.widget.DiffUtil
import com.iti.itp.bazaar.dto.order.Order

class OrdersDiffUtils : DiffUtil.ItemCallback<Order>(){
    override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
        return oldItem == newItem
    }
}