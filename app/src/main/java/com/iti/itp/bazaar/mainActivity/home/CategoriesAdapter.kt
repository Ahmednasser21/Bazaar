package com.iti.itp.bazaar.mainActivity.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.iti.itp.bazaar.databinding.CategoryItemBinding

class CategoriesAdapter(
    val categories: List<CategoryItem>,
    private val onCategoryClickListener: OnCategoryClickListener
) : RecyclerView.Adapter<CategoriesAdapter.CategoriesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriesViewHolder {
        val binding =
            CategoryItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoriesViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoriesViewHolder, position: Int) {
        val categoryItem = categories[position]
        holder.bindView(onCategoryClickListener, categoryItem)
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    class CategoriesViewHolder(val binding: CategoryItemBinding) : ViewHolder(binding.root) {

        fun bindView(onCategoryClickListener: OnCategoryClickListener, categoryItem: CategoryItem) {
            binding.brandImg.setImageResource(categoryItem.image)
            binding.brandName.text = categoryItem.name
            binding.categoryItemContainer.setOnClickListener {
                onCategoryClickListener.onCategoryClick(categoryItem.name)
            }
        }
    }

}