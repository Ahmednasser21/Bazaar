package com.iti.itp.bazaar.settings.ui.addressFragment.view

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.iti.itp.bazaar.dto.CustomerAddress

class SwipeToDeleteAddress(
    private val adapter:AddressAdapter,
    private val onDelete: (CustomerAddress) -> Unit
) :ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        val deletedItem = adapter.currentList[position]
        onDelete(deletedItem)
    }
}
