package com.iti.itp.bazaar.mainActivity.ui.order

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.iti.itp.bazaar.databinding.OrderItemBinding
import com.iti.itp.bazaar.dto.order.Order

class OrdersAdapter : ListAdapter<Order , OrdersAdapter.OrdersViewHolder> (OrdersDiffUtils()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrdersViewHolder {
        val binding = OrderItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return OrdersViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrdersViewHolder, position: Int) {
        val order = getItem(position)
        holder.bindView(order)
    }

    class  OrdersViewHolder(val binding:OrderItemBinding):RecyclerView.ViewHolder(binding.root){
        fun bindView(order:Order){
            binding.tvOrderPrice.text = order.totalPrice
            binding.tvCreationDate.text = order.createdAt
        }
    }

}