package com.iti.itp.bazaar.mainActivity.ui.home

import androidx.recyclerview.widget.DiffUtil
import com.iti.itp.bazaar.dto.smartCollections.SmartCollection

class BrandsDiffUtil : DiffUtil.ItemCallback<SmartCollection>() {
    override fun areItemsTheSame(oldItem: SmartCollection, newItem: SmartCollection): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: SmartCollection, newItem: SmartCollection): Boolean {
        return oldItem == newItem
    }
}