package com.iti.itp.bazaar.mainActivity.ui.home

import androidx.recyclerview.widget.DiffUtil

class BrandsDiffUtil : DiffUtil.ItemCallback<BrandsDTO>() {
    override fun areItemsTheSame(oldItem: BrandsDTO, newItem: BrandsDTO): Boolean {
        return oldItem.vendorName == newItem.vendorName
    }

    override fun areContentsTheSame(oldItem: BrandsDTO, newItem: BrandsDTO): Boolean {
        return oldItem == newItem
    }
}