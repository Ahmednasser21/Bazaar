package com.iti.itp.bazaar.productInfo.view

import androidx.recyclerview.widget.DiffUtil


data class AvailableColor ( var color : String)
class AvailableColorDiffUtill : DiffUtil.ItemCallback<AvailableColor>() {
    override fun areItemsTheSame(oldItem: AvailableColor, newItem: AvailableColor): Boolean {
        return oldItem.color == newItem.color
    }

    override fun areContentsTheSame(oldItem: AvailableColor, newItem: AvailableColor): Boolean {
        return oldItem == newItem
    }
}