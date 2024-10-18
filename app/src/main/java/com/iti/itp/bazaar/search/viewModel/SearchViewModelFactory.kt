package com.iti.itp.bazaar.search.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.productinfoform_commerce.productInfo.viewModel.prouductInfoViewModel
import com.iti.itp.bazaar.repo.Repository

class SearchViewModelFactory (private val repo: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            SearchViewModel(repo ) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}