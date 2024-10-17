package com.iti.itp.bazaar.mainActivity.ui.brand

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.repo.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BrandProductsViewModel(private val repo:Repository) : ViewModel() {

    private val _productStateFlow = MutableStateFlow<DataState>(DataState.Loading)
    val productStateFlow = _productStateFlow.asStateFlow()
    fun getVendorProducts(vendorName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.getVendorProducts(vendorName)
                .catch {e->
                    _productStateFlow.value = DataState.OnFailed(e)
                }
                .collectLatest{
                    _productStateFlow.value = DataState.OnSuccess(it)
                }
        }
    }
}