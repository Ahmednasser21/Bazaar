package com.iti.itp.bazaar.settings.ui.newAddressFragment.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.itp.bazaar.dto.AddAddressResponse
import com.iti.itp.bazaar.dto.AddedAddressRequest
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

    fun addNewAddress(customerId: Long, address: AddAddressResponse) {
        viewModelScope.launch(Dispatchers.IO) {
            addressRepo.addAddress(customerId, address).catch {
                _addressState.value = DataState.OnFailed(it)
                Log.e(TAG, "addNewAddress: failed to add the new address ${it.message}")
            }.collect {
                _addressState.value = DataState.OnSuccess(it)
                Log.i(TAG, "addNewAddress: successfully added the new address")
            }
        }
    }

}