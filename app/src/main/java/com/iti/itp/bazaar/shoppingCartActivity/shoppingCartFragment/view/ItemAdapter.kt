package com.iti.itp.bazaar.shoppingCartActivity.shoppingCartFragment.view

import ReceivedLineItem
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.iti.itp.bazaar.databinding.ShoppingCartItemBinding
import java.text.NumberFormat
import java.util.Locale

class ItemAdapter(
    private val onQuantityChangeListener: OnQuantityChangeListener
) : ListAdapter<ReceivedLineItem, ItemAdapter.ItemViewHolder>(ItemDiffUtil()) {

    class ItemViewHolder(val binding: ShoppingCartItemBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = ShoppingCartItemBinding.inflate(inflater, parent, false)
        return ItemViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentItem = getItem(position)
        val unitPrice = currentItem.price.toDouble() / (currentItem.quantity ?: 1)
        val formatter = NumberFormat.getCurrencyInstance(Locale.US)

        holder.binding.apply {
            tvTitle.text = currentItem.title

            // Update the displayed quantity and price
            val currentQuantity = currentItem.quantity ?: 1
            tvQuantity.text = currentQuantity.toString()
            tvPrice.text = formatter.format(unitPrice * currentQuantity) // Format the total price

            ivIncrease.setOnClickListener {
                val newQuantity = currentQuantity + 1
                val newPrice = unitPrice * newQuantity
                onQuantityChangeListener.onQuantityChanged(currentItem, newQuantity, newPrice)
                Log.i("TAG", "onBindViewHolder: New Price: $newPrice")
            }

            ivDecrease.setOnClickListener {
                if (currentQuantity > 1) {
                    val newQuantity = currentQuantity - 1
                    val newPrice = unitPrice * newQuantity
                    onQuantityChangeListener.onQuantityChanged(currentItem, newQuantity, newPrice)
                }
            }
        }
    }
}

interface OnQuantityChangeListener {
    fun onQuantityChanged(item: ReceivedLineItem, newQuantity: Int, newPrice: Double)
}
