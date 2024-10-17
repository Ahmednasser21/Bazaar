package com.iti.itp.bazaar.mainActivity.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iti.itp.bazaar.databinding.BrandItemBinding
import com.iti.itp.bazaar.dto.smartCollections.SmartCollection

class BrandsAdapter (private val onBrandClickListener: OnBrandClickListener) :
    ListAdapter<SmartCollection, BrandsAdapter.BrandsViewHolder>(BrandsDiffUtil()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BrandsViewHolder {
        val binding = BrandItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BrandsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BrandsViewHolder, position: Int) {
        val smartCollection = getItem(position)
        holder.bind(smartCollection, onBrandClickListener)
    }

    class BrandsViewHolder(private val binding: BrandItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(smartCollection: SmartCollection , onBrandClickListener: OnBrandClickListener) {
            Glide.with(binding.root.context).load(smartCollection.image?.src)
                .into(binding.imgProduct)
            binding.brandName.text = smartCollection.title
            binding.brandItemContainer.setOnClickListener {
                onBrandClickListener.onBrandClick(smartCollection.title)
            }
        }

    }
}