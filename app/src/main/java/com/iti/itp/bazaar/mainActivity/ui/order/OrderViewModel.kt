package com.iti.itp.bazaar.mainActivity.ui.order

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

class OrderViewModel (private val repository:Repository): ViewModel() {

    private val _ordersStateFlow = MutableStateFlow<DataState>(DataState.Loading)
    val ordersStateFlow = _ordersStateFlow.asStateFlow()

    fun getOrdersByCustomerID(query:String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getOrdersByCustomerID(query)
                .catch {e->
                    _ordersStateFlow.value = DataState.OnFailed(e)
                }
                .collectLatest{
                    _ordersStateFlow.value = DataState.OnSuccess(it)
                }
        }
    }

}