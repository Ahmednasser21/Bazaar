package com.iti.itp.bazaar.settings.ui.newAddressFragment.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.itp.bazaar.dto.AddressDetails
import com.iti.itp.bazaar.dto.AddressRequest
import com.iti.itp.bazaar.dto.CustomerAddress
import com.iti.itp.bazaar.mainActivity.ui.DataState

import com.iti.itp.bazaar.repo.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch


class NewAddressViewModel(private val addressRepo: Repository) : ViewModel() {
    private val TAG = "NewAddressViewModel"

    private val _addressState = MutableStateFlow<DataState>(DataState.Loading)
    val addressState = _addressState.asStateFlow()

    fun addNewAddress(customerId: Long, addressDetails: AddressRequest) {
        viewModelScope.launch(Dispatchers.IO) {
            addressRepo.createCustomerAddress(customerId, addressDetails).catch {
                _addressState.value = DataState.OnFailed(it)
                Log.e(TAG, "addNewAddress: failed to add the new address ${it.message}")
            }.collect {
                _addressState.value = DataState.OnSuccess(it)
                Log.i(TAG, "addNewAddress: successfully added the new address")
            }
        }
    }

}