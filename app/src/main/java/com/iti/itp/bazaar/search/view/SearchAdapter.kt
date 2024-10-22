package com.iti.itp.bazaar.search.view

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.iti.itp.bazaar.databinding.AvailableSizeItemBinding
import com.iti.itp.bazaar.databinding.SearchProductItemBinding
import com.iti.itp.bazaar.network.products.Products
import com.iti.itp.bazaar.productInfo.OnClickListner
import com.iti.itp.bazaar.productInfo.OnColorClickListner
import com.iti.itp.bazaar.productInfo.view.AvailableColor
import com.iti.itp.bazaar.productInfo.view.AvailableColorAdapter
import com.iti.itp.bazaar.productInfo.view.AvailableColorAdapter.AvailbleColorsViewHolder
import com.iti.itp.bazaar.productInfo.view.AvailableColorDiffUtill
import com.iti.itp.bazaar.search.OnCardClickListner
import com.iti.itp.bazaar.search.OnSearchProductFavClick

class SearchAdapter ( var  onClick : OnCardClickListner , var onFavClick : OnSearchProductFavClick)  :
    ListAdapter<Products, SearchAdapter.SearchViewHolder>(
        SearchDiffUtill()
    )  {

    lateinit var binding : SearchProductItemBinding
    class SearchViewHolder (val binding : SearchProductItemBinding) : RecyclerView.ViewHolder(binding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchViewHolder {
        val layoutInflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = SearchProductItemBinding.inflate(layoutInflater, parent , false)
        return SearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchViewHolder, position: Int) {
        val currentProduct = getItem(position)

        binding.tvSearchProductName.text = currentProduct.title
        holder.binding.cdSearchProduct.setOnClickListener{
            onClick.onCardClick(currentProduct)
        }
        // for avoiding the display of product without images
        if (!currentProduct.image?.src.isNullOrBlank()) {
            Glide.with(binding.root.context).load(currentProduct.image?.src)
                .into(holder.binding.ivSearchImageProduct)
        }
//        holder.binding.btnAddToFav.setOnClickListener {
//            onFavClick.onFavClick(currentProduct)
//            Log.d("TAG", "onFavClick: click favorite adapter")
//            holder.binding.btnAddToFav.setColorFilter(Color.BLUE)
//        }

    }

    private fun extractProductName(fullName: String): String {
        val delimiter = "|"
        val parts = fullName.split(delimiter)
        return if (parts.size > 1) parts[1].trim() else ""
    }

}