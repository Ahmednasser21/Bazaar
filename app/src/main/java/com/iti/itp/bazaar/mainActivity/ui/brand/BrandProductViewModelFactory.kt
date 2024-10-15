package com.iti.itp.bazaar.mainActivity.ui.brand

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.iti.itp.bazaar.repo.Repository

class BrandProductViewModelFactory (private val repo: Repository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(BrandProductsViewModel::class.java)) {
            BrandProductsViewModel(repo) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}