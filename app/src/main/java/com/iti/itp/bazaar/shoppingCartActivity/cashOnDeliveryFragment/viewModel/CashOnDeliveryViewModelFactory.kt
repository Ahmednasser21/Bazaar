package com.iti.itp.bazaar.shoppingCartActivity.cashOnDeliveryFragment.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.iti.itp.bazaar.repo.CurrencyRepository
import com.iti.itp.bazaar.repo.Repository

class CashOnDeliveryViewModelFactory(val repository: Repository , val currencyRepository: CurrencyRepository):ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(CashOnDeliveryViewModel::class.java)){
            CashOnDeliveryViewModel(repository,currencyRepository) as T
        }else{
            throw IllegalArgumentException("View Model not found")
        }
    }
}