package com.iti.itp.bazaar.mainActivity.ui.me

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.repo.CurrencyRepository
import com.iti.itp.bazaar.repo.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class MeViewModel(private val currencyRepository: CurrencyRepository, private val repository: Repository) : ViewModel() {

    companion object{
        private const val TAG = "NotificationsViewModel"
    }

    private val _currency = MutableStateFlow<DataState>(DataState.Loading)
    val currency = _currency.asStateFlow()

    private val _customer = MutableStateFlow<DataState>(DataState.Loading)
    val customer = _customer.asStateFlow()

    fun getCurrency(base:String, target:String){
        viewModelScope.launch(Dispatchers.IO) {
            currencyRepository.getExchangeRate(base, target).catch {
                _currency.value = DataState.OnFailed(it)
            }.collect{
                _currency.value = DataState.OnSuccess(it)
            }
        }
    }

    fun getCustomerById(customerId:Long){
        viewModelScope.launch(Dispatchers.IO){
            repository.getCustomerById(customerId).catch {
                _customer.value = DataState.OnFailed(it)
                Log.e(TAG, "failed to getCustomerById due to: ${it.message}")
            }.collect{
                _customer.value = DataState.OnSuccess(it)
                Log.i(TAG, "success to getCustomerById: ")
            }
        }
    }
}