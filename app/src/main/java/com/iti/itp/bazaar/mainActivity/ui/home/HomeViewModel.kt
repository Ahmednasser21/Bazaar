package com.iti.itp.bazaar.mainActivity.ui.home

import android.util.Log
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

    companion object{
        private const val TAG = "HomeViewModel"
    }

    private val _brandStateFlow = MutableStateFlow<DataState>(DataState.Loading)
    val brandStateFlow = _brandStateFlow.asStateFlow()

    private val _priceRules = MutableStateFlow<DataState>(DataState.Loading)
    val priceRules = _priceRules.asStateFlow()

    private val _priceRulesCount = MutableStateFlow<DataState>(DataState.Loading)
    val priceRulesCount = _priceRulesCount.asStateFlow()

    fun getVendors() {
        viewModelScope.launch(Dispatchers.IO) {
            repo.getVendors()
                .catch {e->
                    _brandStateFlow.value = DataState.OnFailed(e)
                    Log.e(TAG, "failed to getVendors: ${e.message}")
                }
                .collect{
                    _brandStateFlow.value = DataState.OnSuccess(it)
                    Log.i(TAG, "success to getVendors: $it")
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


    fun getPriceRulesCount(){
        viewModelScope.launch(Dispatchers.IO) {
            repo.getPriceRulesCount().catch {
                _priceRulesCount.value = DataState.OnFailed(it)
                Log.e(TAG, "failed to price rules count: ${it.message}")
            }.collect{
                _priceRulesCount.value = DataState.OnSuccess(it)
                Log.i(TAG, "success to getPriceRulesCount: $it")
            }
        }
    }
}