package com.iti.itp.bazaar.mainActivity.ui.brand

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iti.itp.bazaar.databinding.ProductsItemBinding
import com.iti.itp.bazaar.network.products.Products

class BrandProductsAdapter(
    private val onBrandProductClickListener: OnBrandProductClickListener,
    private val onFavouriteClickListener: OnFavouriteClickListener
) : ListAdapter<Products, BrandProductsAdapter.BrandProductViewHolder>(BrandProductsDiffUtils()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrandProductViewHolder {
        val binding = ProductsItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BrandProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BrandProductViewHolder, position: Int) {
        val product = getItem(position)
        holder.bindView(product, onBrandProductClickListener,onFavouriteClickListener)
    }

    class BrandProductViewHolder(private val binding: ProductsItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindView(
            productDTO: Products,
            onBrandProductClickListener: OnBrandProductClickListener,
            onFavouriteClickListener: OnFavouriteClickListener
        ) {
            binding.tvProductName.text = extractProductName(productDTO.title)
            Glide.with(binding.root.context)
                .load(productDTO.image?.src)
                .into(binding.imgProduct)
            binding.tvProductPrice.text = "${productDTO.variants[0].price} EGP"
            binding.productContainer.setOnClickListener {
                onBrandProductClickListener.onBrandProductClick(productDTO.id)
            }
            binding.imgFav.setOnClickListener{
                onFavouriteClickListener.onFavClick()
            }
        }
        private fun extractProductName(fullName: String): String {
            val delimiter = "|"
            val parts = fullName.split(delimiter)
            return if (parts.size > 1) parts[1].trim() else ""
        }
    }
}
