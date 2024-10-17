package com.iti.itp.bazaar.shoppingCartActivity.cashOnDeliveryFragment.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.network.responses.ExchangeRateResponse
import com.iti.itp.bazaar.repo.CurrencyRepository
import com.iti.itp.bazaar.repo.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class CashOnDeliveryViewModel(val repository: Repository, val currencyRepository: CurrencyRepository):ViewModel() {
    companion object{
        private const val TAG = "CashOnDeliveryViewModel"
    }

    private val _coupons = MutableStateFlow<DataState>(DataState.Loading)
    val coupons = _coupons.asStateFlow()

    private val _currency = MutableStateFlow<DataState>(DataState.Loading)
    val currency = _currency.asStateFlow()


    fun getCoupons(){
        viewModelScope.launch(Dispatchers.IO){
            repository.getPriceRules().catch {
                _coupons.value = DataState.OnFailed(it)
                Log.e(TAG, "Failed to getCoupons because of: ${it.message} ")
            }.collect{
                _coupons.value = DataState.OnSuccess(it)
                Log.i(TAG, "Success to getCoupons")
            }
        }
    }

    fun exchangeCurrency(base:String, target:String){
        viewModelScope.launch(Dispatchers.IO){
            currencyRepository.getExchangeRate(base, target).catch {
                _currency.value = DataState.OnFailed(it)
            }.collect{
                _currency.value = DataState.OnSuccess(it)
            }
        }
    }

}