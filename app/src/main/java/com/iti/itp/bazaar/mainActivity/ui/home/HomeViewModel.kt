package com.iti.itp.bazaar.mainActivity.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.repo.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class HomeViewModel(private val repo: Repository) : ViewModel() {

    private val _brandStateFlow = MutableStateFlow<DataState>(DataState.Loading)
    val brandStateFlow = _brandStateFlow.asStateFlow()

    fun getVendors(fields: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.getVendors(fields)
                .catch {e->
                    _brandStateFlow.value = DataState.OnFailed(e)
                }
                .collect{
                    _brandStateFlow.value = DataState.OnSuccess(it)
                }
        }
    }
}