package com.iti.itp.bazaar.productInfo.view

import androidx.recyclerview.widget.DiffUtil




data class AvailableSizes ( var size : String)
class AvailableSizeDiffUtill : DiffUtil.ItemCallback<AvailableSizes>() {
    override fun areItemsTheSame(oldItem: AvailableSizes, newItem: AvailableSizes): Boolean {
        return oldItem.size == newItem.size
    }

    override fun areContentsTheSame(oldItem: AvailableSizes, newItem: AvailableSizes): Boolean {
        return oldItem == newItem
    }
}
