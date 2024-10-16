package com.iti.itp.bazaar.mainActivity.ui.brand

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iti.itp.bazaar.databinding.BrandProductsBinding
import com.iti.itp.bazaar.network.products.Products

class BrandProductsAdapter(
    private val onBrandProductClickListener: OnBrandProductClickListener
) : ListAdapter<Products, BrandProductsAdapter.BrandProductViewHolder>(BrandProductsDiffUtils()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrandProductViewHolder {
        val binding = BrandProductsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BrandProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BrandProductViewHolder, position: Int) {
        val product = getItem(position)
        holder.bindView(product, onBrandProductClickListener)
    }

    class BrandProductViewHolder(private val binding: BrandProductsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindView(
            productDTO: Products,
            onBrandProductClickListener: OnBrandProductClickListener
        ) {
            binding.tvProductName.text = extractProductName(productDTO.title)
            Glide.with(binding.root.context)
                .load(productDTO.image?.src)
                .into(binding.imgProduct)
            binding.tvProductPrice.text = "${productDTO.variants[0].price} EGP"
            binding.productContainer.setOnClickListener {
                onBrandProductClickListener.onBrandProductClick(productDTO.id)
            }
        }
        private fun extractProductName(fullName: String): String {
            val delimiter = "|"
            val parts = fullName.split(delimiter)
            return if (parts.size > 1) parts[1].trim() else ""
        }
    }
}
