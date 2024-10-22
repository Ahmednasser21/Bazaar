package com.example.productinfoform_commerce.productInfo.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.itp.bazaar.dto.DraftOrderRequest
import com.iti.itp.bazaar.dto.UpdateDraftOrderRequest

import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.mainActivity.ui.home.HomeViewModel
import com.iti.itp.bazaar.mainActivity.ui.home.HomeViewModel.Companion
import com.iti.itp.bazaar.repo.CurrencyRepository
import com.iti.itp.bazaar.repo.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class prouductInfoViewModel (private val repo: Repository , private val currencyRepository: CurrencyRepository) : ViewModel() {
    companion object{
        private const val TAG = "prouductInfoViewModel"
    }

    private val _productDetailsStateFlow = MutableStateFlow<DataState>(DataState.Loading)
    val productDetailsStateFlow = _productDetailsStateFlow.asStateFlow()

    private val _currencyStateFlow = MutableStateFlow<DataState>(DataState.Loading)
    val currencyStateFlow = _currencyStateFlow.asStateFlow()

    private val _createdOrder = MutableStateFlow<DataState>(DataState.Loading)
    val createdOrder = _createdOrder.asStateFlow()

    private val _priceRules = MutableStateFlow<DataState>(DataState.Loading)
    val priceRules = _priceRules.asStateFlow()

    private val _updatedOrder = MutableStateFlow<DataState>(DataState.Loading)
    val updatedOrder = _updatedOrder.asStateFlow()

    private val _allDraftOrders = MutableStateFlow<DataState>(DataState.Loading)
    val allDraftOrders = _allDraftOrders.asStateFlow()


    private val _specificDraftOrders = MutableStateFlow<DataState>(DataState.Loading)
    val specificDraftOrders = _specificDraftOrders.asStateFlow()

    private val _createdFavDraftOrder = MutableStateFlow<DataState>(DataState.Loading)
    val createdFavDraftOrder = _createdFavDraftOrder.asStateFlow()

    private val _allDraftOrdersFav = MutableStateFlow<DataState>(DataState.Loading)
    val allDraftOrdersFav = _allDraftOrdersFav.asStateFlow()

    private val _deleteLineItemFromDraftOrder = MutableStateFlow<DataState>(DataState.Loading)
    val deleteLineItemFromDraftOrder = _deleteLineItemFromDraftOrder.asStateFlow()


    fun getProductDetails(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.getProductDetails (id)
                .catch {e->
                    _productDetailsStateFlow.value = DataState.OnFailed(e)
                    Log.d("TAG", "getProductDetails VIEW MODEL: ${e.printStackTrace()}")
                }
                .collectLatest{
                    _productDetailsStateFlow.value = DataState.OnSuccess(it)
                    Log.d("TAG", "getProductDetails VIEW MODEL: CASE SUCCESS")
                }
        }
    }

    fun getCurrencyRate ( base :String , target : String  ) {
        viewModelScope.launch (Dispatchers.IO){
            currencyRepository.getExchangeRate(base ,target )
                .catch {
                    _currencyStateFlow.value = DataState.OnFailed(it)
                    Log.d("TAG", "getCuurencyRate VIEW MODEL: ${it.printStackTrace()}")
                }
                .collectLatest {
                    _currencyStateFlow.value = DataState.OnSuccess(it)
                    Log.d("TAG", "getCuurencyRate VIEW MODEL: CASE SUCCESS")
                }
        }
    }


    fun createOrder(draftOrderRequest: DraftOrderRequest){
        viewModelScope.launch(Dispatchers.IO){
            repo.createDraftOrder(draftOrderRequest).catch {
                _createdOrder.value = DataState.OnFailed(it)
                Log.e("TAG", "there was error while creating error: ${it.message}")
            }.collect{
                _createdOrder.value = DataState.OnSuccess(it)
                Log.i("TAG", "created the Order successfully")
            }
        }
    }




    fun getPriceRules(){
        viewModelScope.launch(Dispatchers.IO) {
            repo.getPriceRules().catch {
                _priceRules.value = DataState.OnFailed(it)
                Log.e(TAG, "failed to priceRules: ${it.message}")
            }.collect{
                _priceRules.value = DataState.OnSuccess(it)
                Log.i(TAG, "success to getPriceRules: $it")
            }
        }
    }


    fun updateDraftOrder(draftOrderId:Long, updateDraftOrderRequest: UpdateDraftOrderRequest){
        viewModelScope.launch(Dispatchers.IO){
            repo.updateDraftOrderRequest(draftOrderId, updateDraftOrderRequest).catch {
                _updatedOrder.value = DataState.OnFailed(it)
                Log.e(TAG, "error updateDraftOrder: ${it.message}")
            }.collect{
                _updatedOrder.value = DataState.OnSuccess(it)
                Log.i(TAG, "updateDraftOrder: $it")
            }
        }
    }

    fun getAllDraftOrders(){
        viewModelScope.launch(Dispatchers.IO){
            repo.getAllDraftOrders().catch {
                _allDraftOrders.value = DataState.OnFailed(it)
                Log.e(TAG, "error getAllDraftOrders: ${it.message}")
            }.collect{
                _allDraftOrders.value = DataState.OnSuccess(it)
                Log.i(TAG, "success getAllDraftOrders")
            }
        }
    }

    fun getSpecificDraftOrder (draftOrderId: Long) {
        viewModelScope.launch(Dispatchers.IO) {

            repo.getSpecificDraftOrder(draftOrderId).catch {
                _specificDraftOrders.value = DataState.OnFailed(it)
                Log.e(TAG, "error getSpecificDraftOrder: ${it.message}")

            }.collect {
                _specificDraftOrders.value = DataState.OnSuccess(it)
                Log.i(TAG, "success getSpecificDraftOrder")
            }

        }
    }
    fun deleteSpecificDraftOrder (draftOrderId :Long )
    {
        viewModelScope.launch {
            repo.deleteSpecificDraftOrder(draftOrderId)
        }

    }

    fun createFavDraftOrder(draftOrderRequest: DraftOrderRequest){
        viewModelScope.launch(Dispatchers.IO){
            repo.createDraftOrder(draftOrderRequest).catch {
                _createdFavDraftOrder.value = DataState.OnFailed(it)
                Log.e("TAG", "there was error while creating error: ${it.message}")
            }.collect{
                _createdFavDraftOrder.value = DataState.OnSuccess(it)
                Log.i("TAG", "created the Order successfully")
            }
        }
    }


    fun getAllDraftOrdersForFav(){
        viewModelScope.launch(Dispatchers.IO){
            repo.getAllDraftOrders().catch {
                _allDraftOrdersFav.value = DataState.OnFailed(it)
                Log.e(TAG, "error getAllDraftOrdersForFav: ${it.message}")
            }.collect{
                _allDraftOrdersFav.value = DataState.OnSuccess(it)
                Log.i(TAG, "success getAllDraftOrdersForFav")
            }
        }
    }

    fun DeleteLineItemFromDraftOrder(draftOrderId:Long, updateDraftOrderRequest: UpdateDraftOrderRequest){
        viewModelScope.launch(Dispatchers.IO){
            repo.updateDraftOrderRequest(draftOrderId, updateDraftOrderRequest).catch {
                _deleteLineItemFromDraftOrder.value = DataState.OnFailed(it)
                Log.e(TAG, "error DeleteLineItemFromDraftOrder: ${it.message}")
            }.collect{
                _deleteLineItemFromDraftOrder.value = DataState.OnSuccess(it)
                Log.i(TAG, "DeleteLineItemFromDraftOrder: $it")
            }
        }
    }


}