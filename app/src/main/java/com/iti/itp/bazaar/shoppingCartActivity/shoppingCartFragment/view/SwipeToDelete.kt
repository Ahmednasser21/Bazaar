package com.iti.itp.bazaar.shoppingCartActivity.shoppingCartFragment.view

import ReceivedLineItem
import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.iti.itp.bazaar.dto.LineItem

class SwipeToDelete(
    private val adapter: ItemAdapter,
    private val onDelete: (LineItem) -> Unit
) : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean = false

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        val item = adapter.currentList[position]

        MaterialAlertDialogBuilder(viewHolder.itemView.context)
            .setTitle("Remove Item")
            .setMessage("Are you sure you want to remove this item from your cart?")
            .setPositiveButton("Remove") { dialog, _ ->
                dialog.dismiss()
                onDelete(item)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                // Restore the swiped item
                adapter.notifyItemChanged(position)
            }
            .setOnCancelListener {
                // Restore the swiped item if dialog is dismissed
                adapter.notifyItemChanged(position)
            }
            .show()
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }
}