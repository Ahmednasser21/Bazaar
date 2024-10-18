package com.iti.itp.bazaar.productInfo.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.iti.itp.bazaar.databinding.AvailableSizeItemBinding
import com.iti.itp.bazaar.productInfo.OnClickListner

class AvailableSizesAdapter ( var  onClick : OnClickListner<AvailableSizes>) :
    ListAdapter<AvailableSizes, AvailableSizesAdapter.AvailbleSizesViewHolder>(AvailableSizeDiffUtill())  {
    lateinit var binding : AvailableSizeItemBinding


    class AvailbleSizesViewHolder (val binding : AvailableSizeItemBinding) : RecyclerView.ViewHolder(binding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvailbleSizesViewHolder {

        val layoutInflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
       binding = AvailableSizeItemBinding.inflate(layoutInflater, parent , false)
        return AvailbleSizesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AvailbleSizesViewHolder, position: Int) {
        val currentSize = getItem(position)
        holder.binding.tvAvailableSize .text = currentSize.size

        holder.binding.cvAvailableSizes.setOnClickListener{
            onClick.OnClick(currentSize)
        }
    }
}