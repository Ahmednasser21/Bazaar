package com.iti.itp.bazaar.productInfo.view

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.iti.itp.bazaar.databinding.AvailableSizeItemBinding
import com.iti.itp.bazaar.productInfo.OnClickListner
import com.iti.itp.bazaar.productInfo.OnColorClickListner
import com.iti.itp.bazaar.productInfo.view.AvailableSizesAdapter.AvailbleSizesViewHolder

class AvailableColorAdapter ( var  onClick : OnColorClickListner) :
    ListAdapter<AvailableColor, AvailableColorAdapter.AvailbleColorsViewHolder>(AvailableColorDiffUtill()) {

    lateinit var binding : AvailableSizeItemBinding

    class AvailbleColorsViewHolder (val binding : AvailableSizeItemBinding) : RecyclerView.ViewHolder(binding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvailbleColorsViewHolder {
        val layoutInflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = AvailableSizeItemBinding.inflate(layoutInflater, parent , false)
        return AvailbleColorsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AvailbleColorsViewHolder, position: Int) {
        val currentColor = getItem(position)
        holder.binding.tvAvailableSize .text = currentColor.color

        holder.binding.cvAvailableSizes.setOnClickListener{
            onClick.OnColorClick(currentColor)
        }
    }
}