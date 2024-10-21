package com.iti.itp.bazaar.shoppingCartActivity.cashOnDeliveryFragment.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.itp.bazaar.dto.PartialOrder
import com.iti.itp.bazaar.dto.PartialOrder2
import com.iti.itp.bazaar.dto.UpdateDraftOrderRequest
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

    private val _draftOrders = MutableStateFlow<DataState>(DataState.Loading)
    val draftOrders = _draftOrders.asStateFlow()

    private val _specificDraftOrder = MutableStateFlow<DataState>(DataState.Loading)
    val specificDraftOrder = _specificDraftOrder.asStateFlow()

    private val _placedOrder = MutableStateFlow<DataState>(DataState.Loading)
    val placedOrder = _placedOrder.asStateFlow()


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

    fun getAllDraftOrders(){
        viewModelScope.launch(Dispatchers.IO){
            repository.getAllDraftOrders().catch {
                _draftOrders.value = DataState.OnFailed(it)
            }.collect{
                _draftOrders.value = DataState.OnSuccess(it)
            }
        }
    }

    fun updateDraftOrder(draftOrderId:Long,updateDraftOrderRequest: UpdateDraftOrderRequest){
        viewModelScope.launch(Dispatchers.IO){
            repository.updateDraftOrderRequest(draftOrderId, updateDraftOrderRequest).catch {
                Log.e(TAG, "failed to apply the discount: ${it.message}")
            }.collect{
                Log.i(TAG, "updateDraftOrder: success to apply the discount")
            }
        }
    }


    fun getSpecificDraftOrder(draftOrderId:Long){
        viewModelScope.launch(Dispatchers.IO){
            repository.getSpecificDraftOrder(draftOrderId).catch {
                Log.e(TAG, "failed to getSpecificDraftOrder: ")
                _specificDraftOrder.value = DataState.OnFailed(it)
            }.collect{
                Log.i(TAG, "success to getSpecificDraftOrder: ")
                _specificDraftOrder.value = DataState.OnSuccess(it)
            }
        }
    }

    fun createOrder(partialOrder2: PartialOrder2){
        viewModelScope.launch {
            repository.createOrder(partialOrder2).catch {
                Log.e(TAG, "Failed placeOrder: ${it.message}")
                _placedOrder.value = DataState.OnFailed(it)
            }.collect{
                Log.i(TAG, "placeOrder: Success")
                _placedOrder.value = DataState.OnSuccess(it)
            }
        }
    }

}