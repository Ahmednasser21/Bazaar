package com.iti.itp.bazaar.settings.ui.addressFragment.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.repo.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class AddressViewModel(val repository: Repository):ViewModel() {

    private val _addresses = MutableStateFlow<DataState>(DataState.Loading)
    val addresses = _addresses.asStateFlow()


    fun getAddressesForCustomer(customerId:Long){
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAddressForCustomer(customerId)
                .catch {
                    _addresses.value = DataState.OnFailed(it)
                }.collect{
                    _addresses.value = DataState.OnSuccess(it)
                }
        }
    }

}