package com.iti.itp.bazaar.settings.ui.addressFragment.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.iti.itp.bazaar.databinding.AddressItemBinding
import com.iti.itp.bazaar.dto.CustomerAddress

class AddressAdapter(val addressListener:OnAddressClickListener):ListAdapter<CustomerAddress,AddressAdapter.AddressViewHolder>(AddressDiffUtil()) {
    private lateinit var binding:AddressItemBinding

    class AddressViewHolder(val binding:AddressItemBinding):ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = AddressItemBinding.inflate(inflater,parent,false)
        return AddressViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AddressViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.binding.countryValue.text = currentItem.country
        holder.binding.cityValue.text = currentItem.city
        holder.binding.phoneValue.text = currentItem.phone
        holder.binding.addressCardView.setOnClickListener{
            addressListener.onAddressClick(currentItem)
        }
    }
}