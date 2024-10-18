package com.iti.itp.bazaar.settings.ui.settingsFragment.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.iti.itp.bazaar.repo.CurrencyRepository
import com.iti.itp.bazaar.repo.Repository

class SettingsViewModelFactory(val repository: Repository,val exchangeCurrencyRepository: CurrencyRepository):ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(SettingsViewModel::class.java)){
            SettingsViewModel(repository,exchangeCurrencyRepository) as T
        }else{
            throw IllegalArgumentException("View Model not found")
        }
    }
}