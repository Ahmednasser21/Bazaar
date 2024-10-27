package com.iti.itp.bazaar.mainActivity.ui.products

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iti.itp.bazaar.databinding.ProductsItemBinding
import com.iti.itp.bazaar.network.products.Products
import kotlin.random.Random

class ProductsAdapter(
    private val isSale:Boolean,
    private val onProductClickListener: OnProductClickListener,
    private val onFavouriteClickListener: OnFavouriteClickListener
) : ListAdapter<Products, ProductsAdapter.CategoryProductViewHolder>(
    ProductsDiffUtils()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryProductViewHolder {
        val binding =
            ProductsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryProductViewHolder, position: Int) {
        val product = getItem(position)
        holder.bindView(isSale,product, onProductClickListener, onFavouriteClickListener)
    }


    class CategoryProductViewHolder(private val binding: ProductsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindView(
            isSale:Boolean,
            productDTO: Products,
            onProductClickListener: OnProductClickListener,
            onFavouriteClickListener: OnFavouriteClickListener
        ) {
            binding.tvProductName.text = extractProductName(productDTO.title)
            Glide.with(binding.root.context)
                .load(productDTO.image?.src)
                .into(binding.imgProduct)
            binding.tvProductPrice.text = if (productDTO.variants.isNullOrEmpty()){""

            }else{"${productDTO.variants[0].price}EGP"}

            binding.productContainer.setOnClickListener {
                onProductClickListener.onProductClick(productDTO.id)
            }
            binding.imgFav.setOnClickListener {
                onFavouriteClickListener.onFavProductClick()
            }
            binding.productRatingBar.rating = ratingList[Random.nextInt(ratingList.size)]
            binding.ratingOfTen.text = "(${binding.productRatingBar.rating*2})"
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
            }else{
                binding.tvOldPrice.visibility = View.INVISIBLE
            }

        }

        private fun extractProductName(fullName: String): String {
            val delimiter = "|"
            val parts = fullName.split(delimiter)
            return if (parts.size > 1) parts[1].trim() else ""
        }
        private val ratingList = listOf(1.8f,1.4f,2.3f, 3.1f, 3.4f, 4.2f, 4.7f, 4.9f)
        private val discountList = listOf(15.00, 20.00, 30.00, 4.99, 10.00,35.00)

    }
}