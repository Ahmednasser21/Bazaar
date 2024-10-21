package com.iti.itp.bazaar.shoppingCartActivity.shoppingCartFragment.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.productinfoform_commerce.productInfo.viewModel.prouductInfoViewModel
import com.example.productinfoform_commerce.productInfo.viewModel.prouductInfoViewModel.Companion
import com.iti.itp.bazaar.dto.UpdateDraftOrderRequest
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.repo.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ShoppingCartFragmentViewModel(val repository: Repository):ViewModel() {
    companion object{
        private const val TAG = "ShoppingCartFragmentVie"
    }

    private val _allDraftOrders = MutableStateFlow<DataState>(DataState.Loading)
    val allDraftOrders = _allDraftOrders.asStateFlow()

    private val _updatedOrder = MutableStateFlow<DataState>(DataState.Loading)
    val updatedOrder = _updatedOrder.asStateFlow()

    private val _specificDraftOrder = MutableStateFlow<DataState>(DataState.Loading)
    val specificDraftOrder = _specificDraftOrder.asStateFlow()

    fun getAllDraftOrders(){
        viewModelScope.launch(Dispatchers.IO){
            repository.getAllDraftOrders().catch {
                _allDraftOrders.value = DataState.OnFailed(it)
                Log.e(TAG, "error getAllDraftOrders: ${it.message}")
            }.collect{
                _allDraftOrders.value = DataState.OnSuccess(it)
                Log.i(TAG, "success getAllDraftOrders")
            }
        }
    }


    fun updateDraftOrder(draftOrderId:Long, updateDraftOrderRequest: UpdateDraftOrderRequest){
        viewModelScope.launch(Dispatchers.IO){
            repository.updateDraftOrderRequest(draftOrderId, updateDraftOrderRequest).catch {
                _updatedOrder.value = DataState.OnFailed(it)
                Log.e(TAG, "error updateDraftOrder: ${it.message}")
            }.collect{
                _updatedOrder.value = DataState.OnSuccess(it)
                Log.i(TAG, "updateDraftOrder: $it")
            }
        }
}

    fun getSpecificDraftOrder(draftOrderId:Long){
        viewModelScope.launch(Dispatchers.IO){
            repository.getSpecificDraftOrder(draftOrderId).catch {
                _specificDraftOrder.value = DataState.OnFailed(it)
                Log.e(TAG, "failed to getSpecificDraftOrder: ")
            }.collect{
                _specificDraftOrder.value = DataState.OnSuccess(it)
                Log.i(TAG, "success to getSpecificDraftOrder: ")
            }
        }
    }
}