package com.iti.itp.bazaar.settings.ui.addressFragment.view

import androidx.recyclerview.widget.DiffUtil
import com.iti.itp.bazaar.dto.CustomerAddress

class AddressDiffUtil:DiffUtil.ItemCallback<CustomerAddress>() {
    override fun areItemsTheSame(oldItem: CustomerAddress, newItem: CustomerAddress): Boolean {
        return oldItem.first_name == newItem.first_name
    }

    override fun areContentsTheSame(oldItem: CustomerAddress, newItem: CustomerAddress): Boolean {
        return oldItem == newItem
    }

}