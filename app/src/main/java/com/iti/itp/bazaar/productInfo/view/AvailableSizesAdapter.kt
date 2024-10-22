package com.iti.itp.bazaar.productInfo.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.iti.itp.bazaar.R
import com.iti.itp.bazaar.databinding.AvailableSizeItemBinding
import com.iti.itp.bazaar.productInfo.OnClickListner

class AvailableSizesAdapter(var onClick: OnClickListner<AvailableSizes>) :
    ListAdapter<AvailableSizes, AvailableSizesAdapter.AvailbleSizesViewHolder>(AvailableSizeDiffUtill()) {

    private var selectedPosition: Int = RecyclerView.NO_POSITION // To track the selected item
    lateinit var binding: AvailableSizeItemBinding

    class AvailbleSizesViewHolder(val binding: AvailableSizeItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AvailbleSizesViewHolder {
        val layoutInflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = AvailableSizeItemBinding.inflate(layoutInflater, parent, false)
        return AvailbleSizesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AvailbleSizesViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val currentSize = getItem(position)

        // Set the size text
        holder.binding.tvAvailableSize.text = currentSize.size

        // Change card color based on selection state
        if (position == selectedPosition) {
            holder.binding.cvAvailableSizes.setCardBackgroundColor(
                holder.itemView.context.getColor(R.color.darkBrown) // Use your selected color here
            )
        } else {
            holder.binding.cvAvailableSizes.setCardBackgroundColor(
                holder.itemView.context.getColor(R.color.white) // Use your default color here
            )
        }

        // Set onClick listener to handle card selection
        holder.binding.cvAvailableSizes.setOnClickListener {
            // Notify the adapter to refresh the previously selected item
            val previousPosition = selectedPosition
            selectedPosition = position

            notifyItemChanged(previousPosition) // Refresh previous item
            notifyItemChanged(selectedPosition) // Refresh newly selected item

            // Trigger your custom click listener
            onClick.OnClick(currentSize)
        }
    }
}