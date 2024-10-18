package com.iti.itp.bazaar.shoppingCartActivity.shoppingCartFragment.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.iti.itp.bazaar.repo.Repository

class ShoppingCartFragmentViewModelFactory(val repository: Repository):ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(ShoppingCartFragmentViewModel::class.java)){
            ShoppingCartFragmentViewModel(repository) as T
        }else{
            throw IllegalArgumentException("no view model found")
        }
    }
}