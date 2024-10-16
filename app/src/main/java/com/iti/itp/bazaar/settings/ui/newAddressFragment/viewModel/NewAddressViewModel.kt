package com.iti.itp.bazaar.settings.ui.newAddressFragment.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.itp.bazaar.dto.CustomerAddress
import com.iti.itp.bazaar.mainActivity.ui.DataState

import com.iti.itp.bazaar.network.addressApi.AddressRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class NewAddressViewModel(private val addressRepo: AddressRepo) : ViewModel() {
    private val TAG = "NewAddressViewModel"

    private val _addressState = MutableStateFlow<DataState>(DataState.Loading)
    val addressState = _addressState.asStateFlow()

    fun addNewAddress(customerId: Long, customerAddress: CustomerAddress) {
        viewModelScope.launch {
            try {
                val result = addressRepo.createCustomerAddress(customerId, customerAddress)
                result.onSuccess { address ->
                    _addressState.value = DataState.OnSuccess(address)
                    Log.i(TAG, "addNewAddress: Success")
                }.onFailure { error ->
                    _addressState.value = DataState.OnFailed(error)
                    Log.i(TAG, "addNewAddress: Fail because ${error.message}")
                }
            } catch (e: Exception) {
                _addressState.value = DataState.OnFailed(e)
            }
        }
    }
}