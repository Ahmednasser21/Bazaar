package com.iti.itp.bazaar.settings.ui.newAddressFragment.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.iti.itp.bazaar.network.addressApi.AddressRepo

class NewAddressViewModelFactory(val addressRepo: AddressRepo):ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(NewAddressViewModel::class.java)){
            NewAddressViewModel(addressRepo) as T
        }else{
            throw IllegalArgumentException("view model not found")
        }
    }
}