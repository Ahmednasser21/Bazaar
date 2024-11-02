package com.iti.itp.bazaar.mainActivity.shoppingCartFragment.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.core.content.ContextCompat
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
    companion object {
        private const val TAG = "ItemAdapter"
    }
    private lateinit var repository: Repository
    private lateinit var context: Context
    private lateinit var currencySharedPreferences: SharedPreferences
    private var availableTotalAmountInStock: Int? = null
    private var discountPercentage: Double = 0.0

    fun setDiscount(discount: Double) {
        discountPercentage = discount
        notifyDataSetChanged()
    }

    class ItemViewHolder(val binding: ShoppingCartItemBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        context = parent.context
        val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = ShoppingCartItemBinding.inflate(inflater, parent, false)
        repository = Repository.getInstance(ShopifyRemoteDataSource(ShopifyRetrofitObj.productService))
        currencySharedPreferences = parent.context.applicationContext.getSharedPreferences("currencySharedPrefs", Context.MODE_PRIVATE)
        return ItemViewHolder(binding)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val currentItem = getItem(position)
        val sku = currentItem.sku?.split("##")
        val productId = sku?.get(0)
        val size = sku?.get(2)
        val color = sku?.get(1)

        if (productId != "emptySKU") {
            makeNetWorkCallForImage(productId?.toLong() ?: 0, position, holder)

            val baseUnitPrice = currentItem.price.toDouble()
            val currentQuantity = currentItem.quantity ?: 1
            val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
                minimumFractionDigits = 2
                maximumFractionDigits = 2
            }

            holder.binding.apply {
                tvTitle.text = currentItem.title
                colorTv.text = color
                sizeTv.text = size
                tvQuantity.text = currentQuantity.toString()

                // Calculate total price for this item
                val totalPrice = baseUnitPrice * currentQuantity

                if (discountPercentage > 0) {
                    // Calculate discounted price
                    val discountAmount = (totalPrice * discountPercentage) / 100
                    val discountedPrice = totalPrice - discountAmount

                    // Create a SpannableString to show both original and discounted prices
                    val priceText = SpannableStringBuilder().apply {
                        // Original price in primaryColor with strikethrough
                        append("${formatter.format(totalPrice)} EGP ")
                        setSpan(StrikethroughSpan(), 0, length - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                        setSpan(ForegroundColorSpan(ContextCompat.getColor(context, com.denzcoskun.imageslider.R.color.grey_font  )), 0, length - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                        // Arrow
                        append("→ ")

                        // Discounted price in grey
                        val discountedPriceText = formatter.format(discountedPrice)
                        val startIndex = length
                        append(discountedPriceText)
                        setSpan(ForegroundColorSpan(ContextCompat.getColor(context,R.color.primaryColor)), startIndex, length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    }
                    tvPrice.text = priceText
                } else {
                    // Show regular price if no discount
                    tvPrice.text = formatter.format(totalPrice)
                }

                ivIncrease.setOnClickListener {
                    val newQuantity = currentQuantity + 1
                    if (availableTotalAmountInStock != null && newQuantity > availableTotalAmountInStock!!) {
                        Toast.makeText(context, "there is no more in stock", LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    onQuantityChangeListener.onQuantityChanged(currentItem, newQuantity, baseUnitPrice)
                }

                ivDecrease.setOnClickListener {
                    if (currentQuantity > 1) {
                        val newQuantity = currentQuantity - 1
                        onQuantityChangeListener.onQuantityChanged(currentItem, newQuantity, baseUnitPrice)
                    }
                }
            }
        }
    }

    override fun submitList(list: List<LineItem>?) {
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
                        availableTotalAmountInStock = response.products[0].variants.get(0).inventoryQuantity
                        response.products.firstOrNull()?.let { product ->
                            val imageUrl = product.image?.src
                            imageUrl?.let {
                                Glide.with(context)
                                    .load(it)
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
