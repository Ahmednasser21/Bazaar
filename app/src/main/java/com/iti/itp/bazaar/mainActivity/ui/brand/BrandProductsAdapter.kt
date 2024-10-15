package com.iti.itp.bazaar.mainActivity.ui.brand

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iti.itp.bazaar.databinding.BrandProductsBinding

class BrandProductsAdapter(
    private val onBrandProductClickListener: OnBrandProductClickListener
) : ListAdapter<BrandProductDTO, BrandProductsAdapter.BrandProductViewHolder>(BrandProductsDiffUtils()) {

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
            brandProductDTO: BrandProductDTO,
            onBrandProductClickListener: OnBrandProductClickListener
        ) {
            binding.tvProductName.text = brandProductDTO.productName
            Glide.with(binding.root.context)
                .load(brandProductDTO.imgURL)
                .into(binding.imgProduct)
            binding.productContainer.setOnClickListener {
                onBrandProductClickListener.onBrandProductClick(brandProductDTO.productID)
            }
        }
    }
}
