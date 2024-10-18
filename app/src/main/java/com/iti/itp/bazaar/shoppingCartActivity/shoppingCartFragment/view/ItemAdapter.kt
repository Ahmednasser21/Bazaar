package com.iti.itp.bazaar.shoppingCartActivity.shoppingCartFragment.view

import ReceivedLineItem
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.iti.itp.bazaar.databinding.ShoppingCartItemBinding
import com.iti.itp.bazaar.dto.LineItem

class ItemAdapter:ListAdapter<ReceivedLineItem, ItemAdapter.ItemViewHolder>(ItemDiffUtil()) {
    private lateinit var binding:ShoppingCartItemBinding

    class ItemViewHolder(val binding:ShoppingCartItemBinding):ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = ShoppingCartItemBinding.inflate(inflater,parent,false)
        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.binding.tvTitle.text = currentItem.title
        holder.binding.tvPrice.text = currentItem.price
        holder.binding.tvQuantity.text = "${currentItem.quantity}"
    }
}