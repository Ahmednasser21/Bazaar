package com.iti.itp.bazaar.favoriteProducts.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.productinfoform_commerce.productInfo.viewModel.prouductInfoViewModel
import com.iti.itp.bazaar.repo.Repository

class FavoriteProductsViewModelFactory (private val repo: Repository)  : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
       return if (modelClass.isAssignableFrom(FavoriteProductsViewModel::class.java)) {
            FavoriteProductsViewModel(repo) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }


    }
}