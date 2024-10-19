package com.iti.itp.bazaar.shoppingCartActivity.ChooseAdressFragment.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.iti.itp.bazaar.repo.Repository

class ChooseAddressViewModelFactory(val repository: Repository):ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(ChooseAddressViewModel::class.java)){
            ChooseAddressViewModel(repository) as T
        }else{
            throw IllegalArgumentException("no view model found")
        }
    }
}