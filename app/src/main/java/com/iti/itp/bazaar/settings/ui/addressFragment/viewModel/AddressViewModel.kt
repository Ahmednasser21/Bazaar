package com.iti.itp.bazaar.settings.ui.addressFragment.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.itp.bazaar.dto.CustomerAddress
import com.iti.itp.bazaar.dto.CustomerAddressResponse
import com.iti.itp.bazaar.dto.UpdateAddressRequest
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.repo.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class AddressViewModel(val repository: Repository):ViewModel() {
    companion object{
        private const val TAG = "AddressViewModel"
    }

    private val _addresses = MutableStateFlow<DataState>(DataState.Loading)
    val addresses = _addresses.asStateFlow()

    private val _updateAddress = MutableStateFlow<DataState>(DataState.Loading)
    val updateAddress = _updateAddress.asStateFlow()


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

    fun updateAddress(customerId: Long, addressId:Long, customerAddress: CustomerAddressResponse){
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCustomerAddress(customerId,addressId, customerAddress)
                .catch {
                    _updateAddress.value = DataState.OnFailed(it)
                    Log.e(TAG, "Failed to update address")
                }.collect{
                    _updateAddress.value = DataState.OnSuccess(it)
                    Log.i(TAG, "Successfully updated address")
                }
        }
    }

    fun deleteAddressForSpecificCustomer(customerId: Long, addressId: Long){
        viewModelScope.launch(Dispatchers.IO){
            repository.deleteAddressOfSpecificCustomer(customerId,addressId)
        }
    }

}