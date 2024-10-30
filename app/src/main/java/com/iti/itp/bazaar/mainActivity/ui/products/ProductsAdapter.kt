package com.iti.itp.bazaar.mainActivity.ui.products

import android.content.Context
import android.graphics.Paint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iti.itp.bazaar.R
import com.iti.itp.bazaar.auth.MyConstants
import com.iti.itp.bazaar.databinding.ProductsItemBinding
import com.iti.itp.bazaar.network.products.Products
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.repo.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

class ProductsAdapter(
    private val isSale: Boolean,
    private val onProductClickListener: OnProductClickListener,
    private val onFavouriteClickListener: OnFavouriteClickListener
) : ListAdapter<Products, ProductsAdapter.CategoryProductViewHolder>(
    ProductsDiffUtils()
) {
    private lateinit var context: Context
    private lateinit var binding: ProductsItemBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryProductViewHolder {
        binding = ProductsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        context = parent.context
        return CategoryProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryProductViewHolder, position: Int) {
        val product = getItem(position)
        holder.bindView(isSale, product, onProductClickListener, onFavouriteClickListener)
        CoroutineScope(Dispatchers.IO).launch {
            makeNetworkCallForFavoriteDraftOrder(context, product, holder.binding)
        }
    }

    class CategoryProductViewHolder(val binding: ProductsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindView(
            isSale: Boolean,
            productDTO: Products,
            onProductClickListener: OnProductClickListener,
            onFavouriteClickListener: OnFavouriteClickListener
        ) {
            Log.i("TAG", "bindView: ${productDTO.title}")
            binding.tvProductName.text = extractProductName(productDTO.title)
            Glide.with(binding.root.context)
                .load(productDTO.image?.src)
                .into(binding.imgProduct)
            binding.tvProductPrice.text = if (productDTO.variants.isNullOrEmpty()) {
                ""
            } else {
                "${productDTO.variants[0].price}EGP"
            }

            binding.productContainer.setOnClickListener {
                onProductClickListener.onProductClick(productDTO.id)
            }
            binding.imgFav.setOnClickListener {
                onFavouriteClickListener.onFavProductClick()
            }
            binding.productRatingBar.rating = ratingList[Random.nextInt(ratingList.size)]
            binding.ratingOfTen.text = "(${binding.productRatingBar.rating * 2})"
            binding.productVendor.text = productDTO.vendor
            if (isSale) {
                binding.tvOldPrice.apply {
                    text = if (productDTO.variants.isNullOrEmpty()) {
                        ""
                    } else {
                        "${
                            productDTO.variants[0].price.toDouble() + (discountList[Random.nextInt(
                                discountList.size
                            )])
                        }EGP"
                    }
                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }
            } else {
                binding.productContainer.elevation = 2f
                binding.tvOldPrice.visibility = View.INVISIBLE
            }
        }

        private val ratingList = listOf(1.8f, 1.4f, 2.3f, 3.1f, 3.4f, 4.2f, 4.7f, 4.9f)
        private val discountList = listOf(15.00, 20.00, 30.00, 4.99, 10.00, 35.00)

        companion object {
            fun extractProductName(fullName: String): String {
                val delimiter = "|"
                val parts = fullName.split(delimiter)
                // Handle cases where there might be multiple delimiters
                return when {
                    parts.size > 1 -> parts.subList(1, parts.size)
                        .joinToString("|")
                        .trim()
                    else -> fullName.trim()
                }
            }
        }
    }

    private suspend fun makeNetworkCallForFavoriteDraftOrder(
        context: Context,
        product: Products,
        itemBinding: ProductsItemBinding
    ) {
        val sharedPrefs =
            context.getSharedPreferences(MyConstants.MY_SHARED_PREFERANCE, Context.MODE_PRIVATE)
        val favoriteDraftOrderId = sharedPrefs.getString(MyConstants.FAV_DRAFT_ORDERS_ID, "0")
        val repo = Repository.getInstance(ShopifyRemoteDataSource(ShopifyRetrofitObj.productService))

        repo.getSpecificDraftOrder(favoriteDraftOrderId?.toLong() ?: 0).collect { response ->
            val productId = product.id.toString()
            response.draft_order.line_items.forEach { lineItem ->
                val lineItemString = lineItem.sku?.split("##")
                val sku = lineItemString?.get(0)?.trim() ?: return@forEach
                Log.i("TAG", "productid is:$productId and itTitle is:$sku")

                if (productId.equals(sku, ignoreCase = true)) {
                    CoroutineScope(Dispatchers.Main).launch {
                        itemBinding.imgFav.setImageResource(R.drawable.filled_favorite)
                    }
                }
            }
        }
    }
}