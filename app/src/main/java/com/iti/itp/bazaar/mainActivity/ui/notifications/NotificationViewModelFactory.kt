package com.iti.itp.bazaar.mainActivity.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.iti.itp.bazaar.repo.CurrencyRepository
import com.iti.itp.bazaar.repo.Repository

class NotificationViewModelFactory(val currencyRepository: CurrencyRepository,private val repository: Repository):ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(NotificationsViewModel::class.java)){
            NotificationsViewModel(currencyRepository, repository) as T
        }else{
            throw IllegalArgumentException("view model not found")
        }
    }
}