package com.iti.itp.bazaar.mainActivity.products

import android.content.Context
import android.graphics.Paint
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iti.itp.bazaar.R
import com.iti.itp.bazaar.auth.MyConstants
import com.iti.itp.bazaar.databinding.ProductsItemBinding
import com.iti.itp.bazaar.dto.LineItem
import com.iti.itp.bazaar.network.products.Products
import com.iti.itp.bazaar.network.shopify.ShopifyRemoteDataSource
import com.iti.itp.bazaar.network.shopify.ShopifyRetrofitObj
import com.iti.itp.bazaar.repo.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class ProductsAdapter(
    private val isSale: Boolean,
    private val onProductClickListener: OnProductClickListener,
    private val onFavouriteClickListener: OnFavouriteClickListener
) : ListAdapter<Products, ProductsAdapter.CategoryProductViewHolder>(ProductsDiffUtils()) {

    private var lineItems: List<LineItem> = emptyList()
    private var isLineItemsLoaded = false
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private val favoriteProductIds = mutableSetOf<Long>()
    private val TAG = "ProductsAdapter"

    companion object {
        private val productRatings = mutableMapOf<Long, Float>()
        private val ratingList = listOf(1.8f, 1.4f, 2.3f, 3.1f, 3.4f, 4.2f, 4.7f, 4.9f)
        private val discountList = listOf(15.00, 20.00, 30.00, 4.99, 10.00, 35.00)

        fun getRatingForProduct(productId: Long): Float {
            return productRatings.getOrPut(productId) {
                ratingList[Random.nextInt(ratingList.size)]
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryProductViewHolder {
        val binding =
            ProductsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        if (!isLineItemsLoaded) {
            coroutineScope.launch {
                makeNetworkCallForFavoriteDraftOrder(parent.context)
                isLineItemsLoaded = true
            }
        }

        return CategoryProductViewHolder(binding)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onBindViewHolder(holder: CategoryProductViewHolder, position: Int) {
        val product = getItem(position)

        if (favoriteProductIds.contains(product.id)) {
            holder.binding.imgFav.setImageResource(R.drawable.filled_favorite)
        } else {
            holder.binding.imgFav.setImageResource(R.drawable.favorite)
        }

        checkProductInFavorites(product.id.toString(), holder.binding)

        holder.bindView(
            isSale = isSale,
            productDTO = product,
            onProductClickListener = onProductClickListener,
            onFavouriteClickListener = onFavouriteClickListener
        )
    }

    private fun checkProductInFavorites(productId: String, binding: ProductsItemBinding) {
        if (lineItems.isNotEmpty()) {
            lineItems.forEach { lineItem ->
                val sku = lineItem.sku?.split("##")?.getOrNull(0)?.trim()
                if (sku != null && productId.equals(sku, ignoreCase = true)) {
                    binding.imgFav.setImageResource(R.drawable.filled_favorite)
                    favoriteProductIds.add(productId.toLong())
                    return@forEach
                }
            }
        }
    }

    private suspend fun makeNetworkCallForFavoriteDraftOrder(context: Context) {
        try {
            withContext(Dispatchers.IO) {
                val sharedPrefs = context.getSharedPreferences(
                    MyConstants.MY_SHARED_PREFERANCE,
                    Context.MODE_PRIVATE
                )
                val favoriteDraftOrderId = sharedPrefs.getString(
                    MyConstants.FAV_DRAFT_ORDERS_ID,
                    "0"
                )
                val repo = Repository.getInstance(
                    ShopifyRemoteDataSource(ShopifyRetrofitObj.productService)
                )

                repo.getSpecificDraftOrder(favoriteDraftOrderId?.toLong() ?: 0)
                    .collect { response ->
                        withContext(Dispatchers.Main) {
                            lineItems = response.draft_order.line_items
                            notifyDataSetChanged()
                        }
                    }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading favorite draft orders", e)
            lineItems = emptyList()
        }
    }

    inner class CategoryProductViewHolder(val binding: ProductsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @RequiresApi(Build.VERSION_CODES.Q)
        fun bindView(
            isSale: Boolean,
            productDTO: Products,
            onProductClickListener: OnProductClickListener,
            onFavouriteClickListener: OnFavouriteClickListener
        ) {
            binding.tvProductName.text = extractProductName(productDTO.title)
            Glide.with(binding.root.context)
                .load(productDTO.image?.src)
                .into(binding.imgProduct)

            binding.tvProductPrice.text = if (!productDTO.variants.isNullOrEmpty()) {
                "${productDTO.variants[0].price} EGP"
            } else {
                ""
            }

            binding.productContainer.setOnClickListener {
                onProductClickListener.onProductClick(productDTO.id)
            }

            binding.imgFav.setOnClickListener {
                val sharedPrefs =
                    binding.root.context.getSharedPreferences(
                        MyConstants.MY_SHARED_PREFERANCE,
                        Context.MODE_PRIVATE
                    )
                val isGuestMode = sharedPrefs.getString(MyConstants.IS_GUEST, "false") ?: "false"
                if (isGuestMode == "false") {

                    if (favoriteProductIds.contains(productDTO.id)) {
                        binding.imgFav.setImageResource(R.drawable.favorite)
                        favoriteProductIds.remove(productDTO.id)
                    } else {
                        binding.imgFav.setImageResource(R.drawable.filled_favorite)
                        favoriteProductIds.add(productDTO.id)
                    }
                }
                onFavouriteClickListener.onFavProductClick(productDTO)
            }

            val rating = getRatingForProduct(productDTO.id)
            binding.productRatingBar.rating = rating
            binding.ratingOfTen.text = "(${rating * 2})"

            binding.productVendor.text = productDTO.vendor

            if (isSale) {
                setupSalePrice(productDTO)
            } else {
                setupRegularPrice()
            }
        }

        private fun setupSalePrice(productDTO: Products) {
            binding.tvOldPrice.apply {
                visibility = View.VISIBLE
                text = if (!productDTO.variants.isNullOrEmpty()) {
                    val basePrice = productDTO.variants[0].price.toDouble()
                    val discount = discountList[Random.nextInt(discountList.size)]
                    "${basePrice + discount} EGP"
                } else {
                    ""
                }
                paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
        }

        private fun setupRegularPrice() {
            binding.productContainer.elevation = 2f
            binding.tvOldPrice.visibility = View.INVISIBLE
        }

        private fun extractProductName(fullName: String): String {
            val delimiter = "|"
            return fullName.split(delimiter).let { parts ->
                if (parts.size > 1) {
                    parts.subList(1, parts.size).joinToString(delimiter).trim()
                } else {
                    fullName.trim()
                }
            }
        }
    }
}
