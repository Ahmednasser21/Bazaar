package com.iti.itp.bazaar.shoppingCartActivity.ChooseAdressFragment.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iti.itp.bazaar.mainActivity.ui.DataState
import com.iti.itp.bazaar.repo.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class ChooseAddressViewModel(val repository: Repository):ViewModel() {

    private val _addressesOfCustomer = MutableStateFlow<DataState>(DataState.Loading)
    val addressesOfCustomer = _addressesOfCustomer.asStateFlow()

    fun getAddressForCustomer(customerId:Long){
       viewModelScope.launch(Dispatchers.IO){
           repository.getAddressForCustomer(customerId).catch {
               _addressesOfCustomer.value = DataState.OnFailed(it)
           }.collect{
               _addressesOfCustomer.value = DataState.OnSuccess(it)
           }
       }
    }

}