package com.iti.itp.bazaar.mainActivity.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.iti.itp.bazaar.databinding.BrandItemBinding

class BrandsAdapter (private val onBrandClickListener: OnBrandClickListener) :
    ListAdapter<BrandsDTO, BrandsAdapter.BrandsViewHolder>(BrandsDiffUtil()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrandsViewHolder {
        val binding = BrandItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BrandsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BrandsViewHolder, position: Int) {
        val brandsDTO = getItem(position)
        holder.bind(brandsDTO, onBrandClickListener)
    }

    class BrandsViewHolder(private val binding: BrandItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(brandsDTO: BrandsDTO , onBrandClickListener: OnBrandClickListener) {
            binding.imgProduct.setImageResource(brandsDTO.img)
            binding.tvProductName.text = brandsDTO.vendorName
            binding.brandItemContainer.setOnClickListener {
                onBrandClickListener.onBrandClick(brandsDTO.vendorName)
            }
        }

    }
}