package com.iti.itp.bazaar.mainActivity.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.iti.itp.bazaar.repo.CurrencyRepository

class NotificationViewModelFactory(val repository: CurrencyRepository):ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(NotificationsViewModel::class.java)){
            NotificationsViewModel(repository) as T
        }else{
            throw IllegalArgumentException("view model not found")
        }
    }
}