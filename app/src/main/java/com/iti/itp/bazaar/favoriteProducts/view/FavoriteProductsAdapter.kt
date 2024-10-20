package com.iti.itp.bazaar.favoriteProducts.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iti.itp.bazaar.databinding.AvailableSizeItemBinding
import com.iti.itp.bazaar.databinding.FavProductItemBinding
import com.iti.itp.bazaar.dto.LineItem
import com.iti.itp.bazaar.favoriteProducts.OnFavProductCardClick
import com.iti.itp.bazaar.favoriteProducts.OnFavProductDelete


class FavoriteProductsAdapter (var onClick : OnFavProductCardClick , var onDeletClick  :OnFavProductDelete) : ListAdapter<LineItem,FavoriteProductsAdapter.FavProductViewHolder>(FavoriteProductsDiffUtill())   {
    lateinit var binding : FavProductItemBinding
    class FavProductViewHolder (var binding : FavProductItemBinding ): RecyclerView.ViewHolder(binding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavProductViewHolder {

        val layoutInflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = FavProductItemBinding.inflate(layoutInflater, parent , false)
        return FavProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavProductViewHolder, position: Int) {
        val currentFavProduct = getItem(position)
        holder.binding.tvFavProductName.text = currentFavProduct.title

        val message = currentFavProduct.sku?.split("##")

        Glide.with(holder.itemView.context).load(message?.get(1)).into(holder.binding.ivFavImageProduct)

        holder.binding.cdFavProduct.setOnClickListener{
            onClick.onCardClick(message?.get(0)!!.toLong())
        }
        holder.binding.btnDeleteFromFavProducts.setOnClickListener {
            onDeletClick.onFavDelete(currentFavProduct)
        }
    }

}