package com.iti.itp.bazaar.mainActivity.ui.categories

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iti.itp.bazaar.databinding.ProductsItemBinding
import com.iti.itp.bazaar.mainActivity.ui.brand.BrandProductsDiffUtils
import com.iti.itp.bazaar.network.products.Products

class CategoryProductsAdapter(
    private val onProductClickListener: OnProductClickListener,
    private val onFavouriteProductClickListener: OnFavouriteProductClickListener
) : ListAdapter<Products, CategoryProductsAdapter.CategoryProductViewHolder>(
    BrandProductsDiffUtils()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryProductViewHolder {
        val binding =
            ProductsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryProductViewHolder, position: Int) {
        val product = getItem(position)
        holder.bindView(product, onProductClickListener, onFavouriteProductClickListener)
    }


    class CategoryProductViewHolder(private val binding: ProductsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindView(
            productDTO: Products,
            onProductClickListener: OnProductClickListener,
            onFavouriteProductClickListener: OnFavouriteProductClickListener
        ) {
            binding.tvProductName.text = extractProductName(productDTO.title)
            Glide.with(binding.root.context)
                .load(productDTO.image?.src)
                .into(binding.imgProduct)
            binding.tvProductPrice.text = if (productDTO.variants.isNullOrEmpty()){""

            }else{"${productDTO.variants[0].price} EGP"}

            binding.productContainer.setOnClickListener {
                onProductClickListener.onProductClick(productDTO.id)
            }
            binding.imgFav.setOnClickListener {
                onFavouriteProductClickListener.onFavProductClick()
            }
        }

        private fun extractProductName(fullName: String): String {
            val delimiter = "|"
            val parts = fullName.split(delimiter)
            return if (parts.size > 1) parts[1].trim() else ""
        }
    }
}