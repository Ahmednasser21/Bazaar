package com.iti.itp.bazaar.settings.ui.settingsFragment.viewModel

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

class SettingsViewModel(val repository: Repository, val exchangeCurrencyRepository: CurrencyRepository):ViewModel() {

    companion object{
        private const val TAG = "SettingsViewModel"
    }

    private val _addresses = MutableStateFlow<DataState>(DataState.Loading)
    val addresses = _addresses.asStateFlow()

    private val _currency = MutableStateFlow<DataState>(DataState.Loading)
    val currency = _currency.asStateFlow()

    fun getAddressesForCustomer(customerId:Long){
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAddressForCustomer(customerId)
                .catch {
                    _addresses.value = DataState.OnFailed(it)
                    Log.e(TAG, "Failed to get customer addresses")
                }.collect{
                    _addresses.value = DataState.OnSuccess(it)
                    Log.i(TAG, "Successfully got customer addresses")
                }
        }
    }


    fun changeCurrency(base:String, target:String){
        viewModelScope.launch {
            exchangeCurrencyRepository.getExchangeRate(base, target).catch {
                _currency.value = DataState.OnFailed(it)
            }.collect{
                _currency.value = DataState.OnSuccess(it)
            }
        }
    }
}