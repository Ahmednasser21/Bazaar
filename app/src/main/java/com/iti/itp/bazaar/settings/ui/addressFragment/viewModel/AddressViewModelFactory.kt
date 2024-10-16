package com.iti.itp.bazaar.settings.ui.addressFragment.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.iti.itp.bazaar.repo.Repository

class AddressViewModelFactory(val repository: Repository):ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(AddressViewModel::class.java)){
            AddressViewModel(repository) as T
        }else{
            throw IllegalArgumentException("no viewModel found")
        }
    }
}