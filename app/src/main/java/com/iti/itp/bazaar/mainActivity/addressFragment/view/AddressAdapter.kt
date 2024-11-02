package com.iti.itp.bazaar.mainActivity.addressFragment.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.iti.itp.bazaar.R
import com.iti.itp.bazaar.databinding.AddressItemBinding
import com.iti.itp.bazaar.dto.CustomerAddress

class AddressAdapter(private val addressListener: OnAddressClickListener) :
    ListAdapter<CustomerAddress, AddressAdapter.AddressViewHolder>(AddressDiffUtil()) {

    // Track the currently selected position
    private var selectedPosition = -1

    class AddressViewHolder(val binding: AddressItemBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressViewHolder {
        val inflater = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val binding = AddressItemBinding.inflate(inflater, parent, false)
        return AddressViewHolder(binding)
    }

    @SuppressLint("ResourceAsColor", "SetTextI18n")
    override fun onBindViewHolder(holder: AddressViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val currentItem = getItem(position)
        val checkBox = holder.binding.defaultAddress


        checkBox.buttonTintList = ColorStateList.valueOf(R.color.black)

        with(holder.binding) {
            countryValue.text = currentItem.country

            if (currentItem.address1!= null && currentItem.address2 != null){
                cityValue.text = "${capitalizeFirstLetter(currentItem.city?:"unknown")}, ${capitalizeFirstLetter(currentItem.address1)}, ${capitalizeFirstLetter(currentItem.address2)}"
            }else{
                cityValue.text = currentItem.city
            }

            // Remove the listener temporarily to avoid callback loops
            defaultAddress.setOnCheckedChangeListener(null)

            // Set the checked state based on the item's default status
            defaultAddress.isChecked = currentItem.default == true

            // Update selected position if this is the default address
            if (currentItem.default == true && selectedPosition != position) {
                selectedPosition = position
            }

            // Set new listener
            defaultAddress.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked && selectedPosition != position) {
                    // Uncheck previous selection
                    val previousPosition = selectedPosition
                    selectedPosition = position

                    // Notify item changes
                    notifyItemChanged(previousPosition)
                    notifyItemChanged(position)

                    // Notify listener of the change
                    addressListener.onAddressClick(currentItem)
                } else if (!isChecked && selectedPosition == position) {
                    // Prevent unchecking the selected radio button
                    buttonView.isChecked = true
                }
            }
        }
    }

    // Method to update the list with new default address
    fun updateDefaultAddress(updatedAddress: CustomerAddress) {
        val currentList = currentList.toMutableList()

        // Find and update the address
        val position = currentList.indexOfFirst { it.id == updatedAddress.id }
        if (position != -1) {
            // Update the previous default address
            val previousDefaultPosition = currentList.indexOfFirst { it.default == true }
            if (previousDefaultPosition != -1) {
                currentList[previousDefaultPosition] = currentList[previousDefaultPosition].copy(default = false)
            }

            // Update the new default address
            currentList[position] = updatedAddress

            // Submit the updated list
            submitList(currentList)
        }
    }

    private fun capitalizeFirstLetter(input: String): String {
        return if (input.isNotEmpty()) {
            input[0].uppercaseChar() + input.substring(1)
        } else {
            input
        }
    }
}