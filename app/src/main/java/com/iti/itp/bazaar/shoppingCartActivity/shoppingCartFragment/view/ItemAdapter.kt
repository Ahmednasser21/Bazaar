package com.iti.itp.bazaar.shoppingCartActivity.shoppingCartFragment.view

import ReceivedLineItem
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.iti.itp.bazaar.R
import com.iti.itp.bazaar.databinding.ShoppingCartItemBinding
import com.iti.itp.bazaar.dto.LineItem
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.repo.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

class ItemAdapter(
    private val onQuantityChangeListener: OnQuantityChangeListener
) : ListAdapter<LineItem, ItemAdapter.ItemViewHolder>(ItemDiffUtil()) {
    companion object{
        private const val TAG = "ItemAdapter"
    }
    private lateinit var repository: Repository
    private lateinit var context: Context
    private lateinit var currencySharedPreferences: SharedPreferences

    class ItemViewHolder(val binding: ShoppingCartItemBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        context = parent.context
        val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = ShoppingCartItemBinding.inflate(inflater, parent, false)
        repository = Repository.getInstance(ShopifyRemoteDataSource(ShopifyRetrofitObj.productService))
        currencySharedPreferences = parent.context.applicationContext.getSharedPreferences("currencySharedPrefs",Context.MODE_PRIVATE)
        return ItemViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentItem = getItem(position)
        if (currentItem.sku != "emptySKU") {
            makeNetWorkCallForImage(currentItem.sku?.toLong() ?: 0, position, holder)

            val unitPrice = currentItem.price.toDouble() / (currentItem.quantity ?: 1)
            val formatter = NumberFormat.getCurrencyInstance(Locale.US)

            holder.binding.apply {
                tvTitle.text = currentItem.title

                val currentQuantity = currentItem.quantity ?: 1
                tvQuantity.text = currentQuantity.toString()
                tvPrice.text = formatter.format(currentItem.price.toDouble())

                ivIncrease.setOnClickListener {
                    val newQuantity = currentQuantity + 1
                    val newPrice = unitPrice * newQuantity
                    onQuantityChangeListener.onQuantityChanged(currentItem, newQuantity, newPrice)
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

    override fun submitList(list: List<LineItem>?) {
        // Filter the list to exclude items with SKU "emptySKU"
        val filteredList = list?.filter { it.sku != "emptySKU" }
        super.submitList(filteredList)
    }

    private fun makeNetWorkCallForImage(productId: Long, position: Int, holder: ItemViewHolder) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.getProductDetails(productId).catch { e ->
                    Log.e(TAG, "makeNetWorkCallForImage: failed to get productInfo", e)
                }.collect { response ->
                    withContext(Dispatchers.Main) {
                        response.products.firstOrNull()?.let { product ->
                            val imageUrl = product.image?.src
                            imageUrl?.let {
                                Glide.with(context)
                                    .load(it)
                                    .placeholder(R.drawable.arrow_forward_24) // Add a placeholder image
                                    .error(R.drawable.shoe) // Add an error image
                                    .into(holder.binding.imageView)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "makeNetWorkCallForImage: Error loading image", e)
            }
        }
    }
}


interface OnQuantityChangeListener {
    fun onQuantityChanged(item: LineItem, newQuantity: Int, newPrice: Double)
}
