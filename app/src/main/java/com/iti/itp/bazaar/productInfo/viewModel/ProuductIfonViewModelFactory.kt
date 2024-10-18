package com.example.productinfoform_commerce.productInfo.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.iti.itp.bazaar.repo.CurrencyRepository
import com.iti.itp.bazaar.repo.Repository

class ProuductIfonViewModelFactory (private val repo: Repository , private val CurrencyRepo : CurrencyRepository) : ViewModelProvider.Factory  {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(prouductInfoViewModel::class.java)) {
            prouductInfoViewModel(repo , CurrencyRepo) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}